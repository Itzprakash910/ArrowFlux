package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.classic.ClassicSolver
import com.example.game.maze.MazeSolver
import com.example.game.threed.ThreeDSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ArrowFlux", appName)
  }

  @Test
  fun `verify classic level generation`() {
    val level = ClassicSolver.getLevel(1)
    assertEquals(1, level.id)
    assertTrue(level.arrows.isNotEmpty())
    // Ensure at least one arrow can escape
    val escapable = level.arrows.any { ClassicSolver.isArrowFree(it, level.arrows, level.gridSize) }
    assertTrue(escapable)
  }

  @Test
  fun `verify maze level generation`() {
    val level = MazeSolver.getLevel(1)
    assertEquals(1, level.id)
    assertTrue(level.arrows.isNotEmpty())
  }

  @Test
  fun `verify 3d level generation`() {
    val level = ThreeDSolver.getLevel(1)
    assertEquals(1, level.id)
    assertTrue(level.arrows.isNotEmpty())
    assertTrue(level.blocks.isNotEmpty())
  }
}
