package com.example.travel_footprint_android.presentation.components.image_random.utils

/**
 * ImageUtils - 图片随机工具函数
 *
 * 【用途】
 *  - 提供随机获取涂鸦（scrawl）图片资源 ID 的工具函数
 *  - 为图片雨特效（ImageRain）组件提供随机的图片素材
 *
 * 【功能】
 *  - 1. 从 R.drawable 中通过反射获取所有以 "ic_scrawl" 开头的图片资源
 *  - 2. 随机选取其中一个返回其资源 ID
 *  - 3. 若无匹配资源，则降级返回默认图片 R.drawable.ic_scrawl0
 *
 * 【关联组件】
 *  - 被 ImageRandom 组件间接使用（ImageRandom 在图片雨中选择随机图片）
 *  - 依赖 R.drawable 中命名的涂鸦图片资源
 *
 * 【简单实现逻辑】
 *  - 通过 Java 反射获取 R.drawable 类中所有声明的字段
 *  - 过滤字段名以 "ic_scrawl" 开头的字段
 *  - 使用 Kotlin Random 从过滤后的字段列表中随机选取一个
 *  - 调用 Field.getInt(null) 获取静态 int 常量值（即资源 ID）返回
 */

import com.example.travel_footprint_android.R
import kotlin.random.Random

// 从 R.drawable 中随机获取一个以 "ic_scrawl" 开头的涂鸦图片资源 ID
fun getRandomScrawlDrawable(): Int {
    // 通过反射获取 R.drawable 类的所有声明字段
    val fields = R.drawable::class.java.declaredFields
    // 过滤出字段名以 "ic_scrawl" 开头的图片资源
    val scrawlFields = fields.filter { it.name.startsWith("ic_scrawl") }
    return if (scrawlFields.isNotEmpty()) {
        // 随机选取一个涂鸦图片资源并返回其 int 值（资源 ID）
        scrawlFields[Random.nextInt(scrawlFields.size)].getInt(null)
    } else {
        // 降级：若无匹配资源则返回默认图片资源 ID
        R.drawable.ic_scrawl0
    }
}
