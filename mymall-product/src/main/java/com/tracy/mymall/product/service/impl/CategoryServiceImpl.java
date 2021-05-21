package com.tracy.mymall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tracy.mymall.product.common.CategoryLevelEnum;
import com.tracy.mymall.product.service.CategoryBrandRelationService;
import com.tracy.mymall.product.vo.CateLogIndexVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.product.dao.CategoryDao;
import com.tracy.mymall.product.entity.CategoryEntity;
import com.tracy.mymall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    private static final String RELEASE_LOCK_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private Redisson redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listByTree() {
        // 1. 查出所有分类
        List<CategoryEntity> allCategories = baseMapper.selectList(null);
        // 2. 找出父子结构

        // 2.1 stream过滤出一层结构，category中添加下层列表结构，通过递归获取
        List<CategoryEntity> firstLevelProducts = allCategories.stream()
                .filter((entity) -> entity.getParentCid().equals(CategoryLevelEnum.DEFAULT.getNumber()))
                .map(currentCategory -> {
                    currentCategory.setCategories(getSubCategories(currentCategory, allCategories));
                    return currentCategory;
                })
                .sorted((entity1, entity2) -> (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort()))
                .collect(Collectors.toList());
        return firstLevelProducts;
    }

    /**
     * 自定义的批量删除方法，删除前会有其他一些动作
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 删除前会有其他一些动作
        baseMapper.deleteBatchIds(asList);
    }



    /**
     * 递归获取层级结构,递归结束的条件是filter条件筛选不到数据
     * @param currentCategory
     * @param allCategories
     * @return
     */
    private List<CategoryEntity> getSubCategories(CategoryEntity currentCategory, List<CategoryEntity> allCategories) {

        List<CategoryEntity> subCategories = allCategories.stream()
                .filter(entity -> entity.getParentCid().equals(currentCategory.getCatId()))
                .map((entity) -> {
                    entity.setCategories(getSubCategories(entity, allCategories));
                    return entity;
                })
                .sorted((entity1, entity2) -> (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort()))
                .collect(Collectors.toList());
        return subCategories;
    }

    /**
     * 获取分类层级，例如[2,5,255] /数码/手机/品牌
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCategoryPath(Long catelogId) {
        List<Long> categoryPathList = new ArrayList<>();
        categoryPathList.add(catelogId);
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        // while循环遍历查询
        while (categoryEntity != null && categoryEntity.getParentCid() != 0) {
            categoryPathList.add(categoryEntity.getParentCid());
            categoryEntity = baseMapper.selectById(categoryEntity.getParentCid());

        }

        Collections.reverse(categoryPathList);

        return categoryPathList.toArray(new Long[categoryPathList.size()]);
    }

    /**
     * 级联更新
     * @param categoryEntity
     */
    @Override
    @Transactional
    public void updateCascade(CategoryEntity categoryEntity) {
        // 细粒度写锁锁，保证缓存一致性
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("catelogIndex-lock");

        RLock lock = readWriteLock.writeLock();
        lock.lock();
        try {
            baseMapper.updateById(categoryEntity);
            if (StringUtils.isNotEmpty(categoryEntity.getName())) {
                categoryBrandRelationService.updateCategoryName(categoryEntity.getCatId(), categoryEntity.getName());
            }
            // 如果数据更新完毕，则删除缓存中的数据，让读取时候重新加载
            stringRedisTemplate.delete("catelogIndex");
        }catch(Exception e) {
            log.error("", e);
        }finally {
            lock.unlock();
        }

    }

    @Override
    //如果缓存中有数据，则在查询时候不执行方法，直接从缓存中拿数据，否则将方法执行结果缓存到firstLevelCategory分组下，
    //可以指定redis中缓存的key值,key可以使用esl表达式
    @Cacheable(value = "firstLevelCategory", key="#root.method.name")
    public List<CategoryEntity> getFirsetLevelCategory() {
        System.out.println("数据没有缓存，进来了");
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", CategoryLevelEnum.DEFAULT.getNumber()));
        return categoryEntities;
    }

    @Override
    // TODO springboot-data-redis默认使用的Lettuce客户端可能产生堆外内存溢出，directMemory
    public Map<String, List<CateLogIndexVo>> getCateLogIndexJson() {
        // 1.先从缓存中查
        String catelogIndex = stringRedisTemplate.opsForValue().get("catelogIndex");
        // 如果缓存中没有，则查询数据库
        if (StringUtils.isEmpty(catelogIndex)) {
            return getCateLogIndexByDistributeRedissoLock();
        }
        System.out.println("我没进来，嘿嘿嘿嘿嘿嘿嘿嘿");
        // 反序列化
        Map<String, List<CateLogIndexVo>> stringListMap = JSON.parseObject(catelogIndex, new TypeReference<Map<String, List<CateLogIndexVo>>>() {});
        return stringListMap;
    }


    /**
     *  使用redisson实现分布式锁，比redis原生简单许多,我们自己不需要考虑续期，原子性等问题
     *  使用读锁配和数据修改地方的写锁实现缓存一致性
     * @return
     */
    private Map<String, List<CateLogIndexVo>> getCateLogIndexByDistributeRedissoLock() {
        Map<String, List<CateLogIndexVo>> stringListMap = null;
        String uuid = UUID.randomUUID().toString();
        // 细粒度加读锁锁
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("catelogIndex-lock");

        RLock lock = readWriteLock.readLock();
        lock.lock();
        try {
            String catelogIndex = stringRedisTemplate.opsForValue().get("catelogIndex");
            // 双重检查
            if (StringUtils.isEmpty(catelogIndex)) {
                stringListMap = queryCateLogIndexFromDb();
                // 没有就写入null值，防止溢出 TODO 处理null值
                String redisValue = "null";
                // 存入redis，加过期时间
                if (stringListMap != null && !stringListMap.isEmpty()) {
                    redisValue = JSON.toJSONString(stringListMap);
                }
                stringRedisTemplate.opsForValue().set("catelogIndex", redisValue, 30 * 60 , TimeUnit.SECONDS);
            }else {
                stringListMap = JSON.parseObject(catelogIndex, new TypeReference<Map<String, List<CateLogIndexVo>>>() {});
            }
            stringRedisTemplate.execute(new DefaultRedisScript<Long>(RELEASE_LOCK_LUA_SCRIPT,Long.class),Collections.singletonList("lock"), uuid);
            return stringListMap;
        }catch(Exception e) {
            log.error("", e);
        }finally {
            lock.unlock();
        }

        return null;

    }


    /**
     *  分布式锁获取数据库内容
     * @return
     */
    private Map<String, List<CateLogIndexVo>> getCateLogIndexByDistributeLock() {
        Map<String, List<CateLogIndexVo>> stringListMap = null;
        String uuid = UUID.randomUUID().toString();
        // 原子加锁，设置有效期
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, Duration.ofSeconds(30));
        if (aBoolean) {
            String catelogIndex = stringRedisTemplate.opsForValue().get("catelogIndex");
            // 双重检查
            if (StringUtils.isEmpty(catelogIndex)) {
                stringListMap = queryCateLogIndexFromDb();
                // 没有就写入null值，防止溢出 TODO 处理null值
                String redisValue = "null";
                // 存入redis，加过期时间
                if (stringListMap != null && !stringListMap.isEmpty()) {
                    redisValue = JSON.toJSONString(stringListMap);
                }
                stringRedisTemplate.opsForValue().set("catelogIndex", redisValue, 30 * 60 , TimeUnit.SECONDS);
            }else {
                stringListMap = JSON.parseObject(catelogIndex, new TypeReference<Map<String, List<CateLogIndexVo>>>() {});
            }
            stringRedisTemplate.execute(new DefaultRedisScript<Long>(RELEASE_LOCK_LUA_SCRIPT,Long.class),Collections.singletonList("lock"), uuid);
            return stringListMap;
        }else {
            // 获取锁失败，则重试
            try{
                Thread.sleep(200L);
            }catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            getCateLogIndexByDistributeLock();
        }

        return null;

    }


    public Map<String, List<CateLogIndexVo>> queryCateLogIndexFromDb() {
        System.out.println("我进来了，打我啊，傻瓜");
        // 查询出所有的记录
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        // 1. 查询一级分类
        List<CategoryEntity> firsetLevelCategory = this.getCategoriesByParentId(categoryEntities, 0L);
        Map<String, List<CateLogIndexVo>> collect = firsetLevelCategory
                .stream()
                // 将数据收集成map
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    // 根据一级分类查询二级分类
                    List<CategoryEntity> secondLevel = this.getCategoriesByParentId(categoryEntities, v.getCatId());
                    List<CateLogIndexVo> cateLogIndexVos = new ArrayList<>();
                    if (secondLevel != null) {
                        // 根据二级分类查询三级分类
                        cateLogIndexVos = secondLevel.stream().map(item -> {
                            CateLogIndexVo cateLogIndexVo = new CateLogIndexVo();
                            cateLogIndexVo.setCatalog1Id(v.getCatId());
                            cateLogIndexVo.setId(item.getCatId());
                            cateLogIndexVo.setName(item.getName());
                            List<CategoryEntity> thirdLevel =  this.getCategoriesByParentId(categoryEntities, item.getCatId());
                            List<CateLogIndexVo.CateLog3Vo> cateLog3Vos = new ArrayList<>();
                            if (thirdLevel != null && !thirdLevel.isEmpty()) {
                                // 组装3级分类信息
                                cateLog3Vos = thirdLevel.stream().map(level -> {
                                    CateLogIndexVo.CateLog3Vo cateLog3Vo = new CateLogIndexVo.CateLog3Vo();
                                    cateLog3Vo.setCatalog2Id(item.getCatId());
                                    cateLog3Vo.setId(level.getCatId());
                                    cateLog3Vo.setName(level.getName());
                                    return cateLog3Vo;
                                }).collect(Collectors.toList());
                            }
                            cateLogIndexVo.setCatalog3List(cateLog3Vos);
                            return cateLogIndexVo;
                        }).collect(Collectors.toList());
                    }
                    return cateLogIndexVos;

                }));
        return collect;
    }
    public List<CategoryEntity> getCategoriesByParentId(List<CategoryEntity> allCategories, Long parentId) {
        List<CategoryEntity> collect = allCategories.stream().filter(item -> item.getParentCid().equals(parentId)).collect(Collectors.toList());
        return collect;
    }

}