package com.example.mytetris.model

import android.graphics.Color

/**
 * 游戏网格类，管理游戏区域的状态和操作
 */
class Grid(
    val width: Int = 10,
    val height: Int = 20
) {
    // 游戏网格，null表示空格，非null表示有方块（存储颜色值）
    private var grid = Array(height) { Array<Int?>(width) { null } }
    
    // 当前活动的方块
    private var currentTetromino: Tetromino? = null
    private var currentX = 0
    private var currentY = 0
    
    // 下一个方块
    private var nextTetromino: Tetromino = Tetromino.random()
    
    // 游戏是否结束
    var isGameOver = false
        private set
    
    // 当前分数
    var score = 0
        private set
    
    // 初始化游戏
    fun start() {
        grid.forEach { row -> row.fill(null) }
        isGameOver = false
        score = 0
        spawnNewTetromino()
    }
    
    // 生成新方块
    private fun spawnNewTetromino() {
        currentTetromino = nextTetromino
        nextTetromino = Tetromino.random()
        
        // 设置初始位置（居中）
        currentX = (width - currentTetromino!!.getCurrentShape()[0].size) / 2
        currentY = 0
        
        // 检查是否可以放置新方块
        if (!isValidPosition()) {
            isGameOver = true
        }
    }
    
    // 检查当前位置是否有效
    private fun isValidPosition(): Boolean {
        val shape = currentTetromino?.getCurrentShape() ?: return false
        for (y in shape.indices) {
            for (x in shape[y].indices) {
                if (shape[y][x]) {
                    val gridX = currentX + x
                    val gridY = currentY + y
                    if (gridX < 0 || gridX >= width || gridY >= height ||
                        (gridY >= 0 && grid[gridY][gridX] != null)) {
                        return false
                    }
                }
            }
        }
        return true
    }
    
    // 移动方块
    fun move(dx: Int, dy: Int): Boolean {
        if (isGameOver) return false
        
        currentX += dx
        currentY += dy
        
        if (!isValidPosition()) {
            // 如果移动无效，恢复位置
            currentX -= dx
            currentY -= dy
            
            // 如果是向下移动失败，则固定方块
            if (dy > 0) {
                lockTetromino()
                clearLines()
                spawnNewTetromino()
            }
            return false
        }
        return true
    }
    
    // 旋转方块
    fun rotate(): Boolean {
        if (isGameOver) return false
        
        currentTetromino?.rotateRight()
        if (!isValidPosition()) {
            currentTetromino?.rotateLeft()
            return false
        }
        return true
    }
    
    // 固定当前方块到网格中
    private fun lockTetromino() {
        val shape = currentTetromino?.getCurrentShape() ?: return
        val color = currentTetromino?.color ?: return
        
        for (y in shape.indices) {
            for (x in shape[y].indices) {
                if (shape[y][x]) {
                    val gridY = currentY + y
                    val gridX = currentX + x
                    if (gridY >= 0) {
                        grid[gridY][gridX] = color
                    }
                }
            }
        }
    }
    
    // 清除完整的行
    private fun clearLines() {
        var linesCleared = 0

        var y = height - 1
        while (y >= 0) {
            if (grid[y].all { it != null }) {
                // 移除该行
                for (y2 in y downTo 1) {
                    for (x in 0 until width) {
                        grid[y2][x] = grid[y2 - 1][x]
                    }
                }
                // 清空顶行
                for (x in 0 until width) {
                    grid[0][x] = null
                }
                linesCleared++
                // 由于删除了一行，需要重新检查当前行
                y++
            }
            y--
        }

        // 更新分数
        when (linesCleared) {
            1 -> score += 100
            2 -> score += 300
            3 -> score += 500
            4 -> score += 800
        }
    }
    
    // 获取网格状态
    fun getGridState(): Array<Array<Int?>> {
        val state = Array(height) { y ->
            Array(width) { x ->
                grid[y][x]
            }
        }
        
        // 添加当前活动方块
        currentTetromino?.let { tetromino ->
            val shape = tetromino.getCurrentShape()
            val color = tetromino.color
            for (y in shape.indices) {
                for (x in shape[y].indices) {
                    if (shape[y][x]) {
                        val gridY = currentY + y
                        val gridX = currentX + x
                        if (gridY >= 0 && gridY < height && gridX >= 0 && gridX < width) {
                            state[gridY][gridX] = color
                        }
                    }
                }
            }
        }
        
        return state
    }
    
    // 获取下一个方块
    fun getNextTetromino(): Tetromino = nextTetromino
} 