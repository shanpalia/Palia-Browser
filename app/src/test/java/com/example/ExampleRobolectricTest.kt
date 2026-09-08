package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.download.utils.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Palia Browser", appName)
  }

  @Test
  fun `test file size formatting`() {
    assertEquals("0 B", FileUtils.formatFileSize(0))
    assertEquals("512 B", FileUtils.formatFileSize(512))
    assertEquals("1.0 KB", FileUtils.formatFileSize(1024))
    assertEquals("1.00 MB", FileUtils.formatFileSize(1024 * 1024))
    assertEquals("1.50 GB", FileUtils.formatFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
  }

  @Test
  fun `test download speed formatting`() {
    assertEquals("0 B/s", FileUtils.formatSpeed(0))
    assertEquals("500 KB/s", FileUtils.formatSpeed(500 * 1024))
    assertEquals("2.4 MB/s", FileUtils.formatSpeed((2.4 * 1024 * 1024).toLong()))
  }

  @Test
  fun `test ETA formatting`() {
    assertEquals("--:-- remaining", FileUtils.formatEta(-1))
    assertEquals("00:45 remaining", FileUtils.formatEta(45))
    assertEquals("06:42 remaining", FileUtils.formatEta(6 * 60 + 42))
    assertEquals("01:15:30 remaining", FileUtils.formatEta(3600 + 15 * 60 + 30))
  }
}

