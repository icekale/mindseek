package com.mindseek.podcast.domain.usecase.offline

import android.content.Context
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

data class StorageInfo(
    val totalSpace: Long,
    val availableSpace: Long,
    val usedByApp: Long,
    val usedByDownloads: Long
)

class GetStorageInfoUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Get storage information including total, available, and used space
     */
    suspend operator fun invoke(): StorageInfo {
        val externalFilesDir = context.getExternalFilesDir(null)
        val downloadDir = File(externalFilesDir, "downloads")
        
        // Get filesystem stats
        val statFs = StatFs(externalFilesDir?.absolutePath ?: context.filesDir.absolutePath)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val availableBlocks = statFs.availableBlocksLong
        
        val totalSpace = totalBlocks * blockSize
        val availableSpace = availableBlocks * blockSize
        
        // Calculate space used by app
        val appUsedSpace = calculateDirectorySize(externalFilesDir)
        
        // Calculate space used by downloads specifically
        val downloadsUsedSpace = if (downloadDir.exists()) {
            calculateDirectorySize(downloadDir)
        } else {
            0L
        }
        
        return StorageInfo(
            totalSpace = totalSpace,
            availableSpace = availableSpace,
            usedByApp = appUsedSpace,
            usedByDownloads = downloadsUsedSpace
        )
    }
    
    /**
     * Calculate the total size of a directory and its contents
     */
    private fun calculateDirectorySize(directory: File?): Long {
        if (directory == null || !directory.exists()) return 0L
        
        var size = 0L
        try {
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        } catch (e: Exception) {
            // Handle permission errors or other issues
        }
        return size
    }
}