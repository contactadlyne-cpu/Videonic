package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.VideoItem
import org.junit.Assert.assertEquals
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
    assertEquals("Videonic Lite", appName)
  }

  @Test
  fun `video item duration formatting`() {
    val video = VideoItem(
      id = 1,
      uri = Uri.parse("content://media/external/video/media/1"),
      title = "Sample Video",
      displayName = "Sample.mp4",
      durationMs = 272000L, // 4m 32s
      sizeBytes = 1288490188L, // ~1.2 GB
      width = 3840,
      height = 2160,
      mimeType = "video/mp4",
      dateAdded = 1700000000L,
      folderName = "Camera",
      path = "/DCIM/Camera/Sample.mp4"
    )

    assertEquals("04:32", video.formattedDuration)
    assertEquals("1.2 GB", video.formattedSize)
    assertEquals("4K", video.resolutionLabel)
  }
}
