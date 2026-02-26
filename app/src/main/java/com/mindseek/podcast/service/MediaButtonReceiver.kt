package com.mindseek.podcast.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.media3.session.MediaButtonReceiver

/**
 * 媒体按钮接收�?- 处理蓝牙耳机和其他媒体按钮事�?
 */
class MediaButtonReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_MEDIA_BUTTON -> {
                val keyEvent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                }
                
                keyEvent?.let { event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        handleMediaButtonEvent(context, event.keyCode)
                    }
                }
            }
            
            // 处理其他媒体相关的广�?
            "android.intent.action.HEADSET_PLUG" -> {
                handleHeadsetEvent(context, intent)
            }
            
            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" -> {
                handleBluetoothHeadsetEvent(context, intent)
            }
        }
    }
    
    private fun handleMediaButtonEvent(context: Context, keyCode: Int) {
        val serviceIntent = Intent(context, AudioPlayerService::class.java)
        
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                serviceIntent.action = AudioPlayerService.ACTION_PLAY
                context.startService(serviceIntent)
            }
            
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                serviceIntent.action = AudioPlayerService.ACTION_PAUSE
                context.startService(serviceIntent)
            }
            
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                // 需要检查当前播放状态来决定是播放还是暂�?
                // 这里简化处理，发送播放命令，服务内部会处理切换逻辑
                serviceIntent.action = AudioPlayerService.ACTION_PLAY
                context.startService(serviceIntent)
            }
            
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                serviceIntent.action = AudioPlayerService.ACTION_NEXT
                context.startService(serviceIntent)
            }
            
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                serviceIntent.action = AudioPlayerService.ACTION_PREVIOUS
                context.startService(serviceIntent)
            }
            
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                serviceIntent.action = AudioPlayerService.ACTION_STOP
                context.startService(serviceIntent)
            }
            
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                // 耳机线控按钮，通常用于播放/暂停
                serviceIntent.action = AudioPlayerService.ACTION_PLAY
                context.startService(serviceIntent)
            }
        }
    }
    
    private fun handleHeadsetEvent(context: Context, intent: Intent) {
        val state = intent.getIntExtra("state", -1)
        val name = intent.getStringExtra("name")
        
        when (state) {
            0 -> {
                // 耳机断开连接
                // 可以选择暂停播放
                val serviceIntent = Intent(context, AudioPlayerService::class.java).apply {
                    action = AudioPlayerService.ACTION_PAUSE
                }
                context.startService(serviceIntent)
            }
            
            1 -> {
                // 耳机连接
                // 可以在这里做一些初始化工作
            }
        }
    }
    
    private fun handleBluetoothHeadsetEvent(context: Context, intent: Intent) {
        // 处理蓝牙耳机连接状态变�?
        // 这里可以根据需要添加相应的逻辑
    }
}