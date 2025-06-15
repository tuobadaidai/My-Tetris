package com.example.mytetris.model

import android.graphics.Color

/**
 * 定义俄罗斯方块的基本形状和属性
 */
class Tetromino(
    val shape: Array<BooleanArray>,
    val color: Int,
    val type: Type
) {
    // 当前旋转状态（0-3，表示旋转次数）
    private var rotation = 0
    
    // 获取当前形状（考虑旋转）
    fun getCurrentShape(): Array<BooleanArray> {
        var currentShape = shape
        repeat(rotation) {
            currentShape = rotateRight(currentShape)
        }
        return currentShape
    }
    
    // 向右旋转
    fun rotateRight() {
        rotation = (rotation + 1) % 4
    }
    
    // 向左旋转
    fun rotateLeft() {
        rotation = (rotation + 3) % 4
    }
    
    // 矩阵右旋转90度
    private fun rotateRight(matrix: Array<BooleanArray>): Array<BooleanArray> {
        val n = matrix.size
        val result = Array(n) { BooleanArray(n) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                result[j][n - 1 - i] = matrix[i][j]
            }
        }
        return result
    }
    
    // 获取方块类型
    enum class Type {
        I, J, L, O, S, T, Z
    }
    
    companion object {
        // 预定义所有方块形状
        val SHAPES = mapOf(
            Type.I to Tetromino(
                arrayOf(
                    booleanArrayOf(false, false, false, false),
                    booleanArrayOf(true, true, true, true),
                    booleanArrayOf(false, false, false, false),
                    booleanArrayOf(false, false, false, false)
                ),
                Color.CYAN,
                Type.I
            ),
            Type.J to Tetromino(
                arrayOf(
                    booleanArrayOf(true, false, false),
                    booleanArrayOf(true, true, true),
                    booleanArrayOf(false, false, false)
                ),
                Color.BLUE,
                Type.J
            ),
            Type.L to Tetromino(
                arrayOf(
                    booleanArrayOf(false, false, true),
                    booleanArrayOf(true, true, true),
                    booleanArrayOf(false, false, false)
                ),
                Color.rgb(255, 165, 0), // Orange
                Type.L
            ),
            Type.O to Tetromino(
                arrayOf(
                    booleanArrayOf(true, true),
                    booleanArrayOf(true, true)
                ),
                Color.YELLOW,
                Type.O
            ),
            Type.S to Tetromino(
                arrayOf(
                    booleanArrayOf(false, true, true),
                    booleanArrayOf(true, true, false),
                    booleanArrayOf(false, false, false)
                ),
                Color.GREEN,
                Type.S
            ),
            Type.T to Tetromino(
                arrayOf(
                    booleanArrayOf(false, true, false),
                    booleanArrayOf(true, true, true),
                    booleanArrayOf(false, false, false)
                ),
                Color.MAGENTA,
                Type.T
            ),
            Type.Z to Tetromino(
                arrayOf(
                    booleanArrayOf(true, true, false),
                    booleanArrayOf(false, true, true),
                    booleanArrayOf(false, false, false)
                ),
                Color.RED,
                Type.Z
            )
        )
        
        // 随机生成一个方块
        fun random(): Tetromino {
            val type = Type.values().random()
            return SHAPES[type]!!
        }
    }
} 