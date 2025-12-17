@file:OptIn(InternalResourceApi::class)

package ecogardengame.composeapp.generated.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String = "composeResources/ecogardengame.composeapp.generated.resources/"

internal val Res.drawable.compose_multiplatform: DrawableResource by lazy {
      DrawableResource("drawable:compose_multiplatform", setOf(
        ResourceItem(setOf(), "${MD}drawable/compose-multiplatform.xml", -1, -1),
      ))
    }

internal val Res.drawable.green_apple: DrawableResource by lazy {
      DrawableResource("drawable:green_apple", setOf(
        ResourceItem(setOf(), "${MD}drawable/green_apple.png", -1, -1),
      ))
    }

internal val Res.drawable.green_apple_half: DrawableResource by lazy {
      DrawableResource("drawable:green_apple_half", setOf(
        ResourceItem(setOf(), "${MD}drawable/green_apple_half.png", -1, -1),
      ))
    }

internal val Res.drawable.orange: DrawableResource by lazy {
      DrawableResource("drawable:orange", setOf(
        ResourceItem(setOf(), "${MD}drawable/orange.png", -1, -1),
      ))
    }

internal val Res.drawable.orange_half: DrawableResource by lazy {
      DrawableResource("drawable:orange_half", setOf(
        ResourceItem(setOf(), "${MD}drawable/orange_half.png", -1, -1),
      ))
    }

internal val Res.drawable.red_apple: DrawableResource by lazy {
      DrawableResource("drawable:red_apple", setOf(
        ResourceItem(setOf(), "${MD}drawable/red_apple.png", -1, -1),
      ))
    }

internal val Res.drawable.red_apple_half: DrawableResource by lazy {
      DrawableResource("drawable:red_apple_half", setOf(
        ResourceItem(setOf(), "${MD}drawable/red_apple_half.png", -1, -1),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainDrawable0Resources(map: MutableMap<String, DrawableResource>) {
  map.put("compose_multiplatform", Res.drawable.compose_multiplatform)
  map.put("green_apple", Res.drawable.green_apple)
  map.put("green_apple_half", Res.drawable.green_apple_half)
  map.put("orange", Res.drawable.orange)
  map.put("orange_half", Res.drawable.orange_half)
  map.put("red_apple", Res.drawable.red_apple)
  map.put("red_apple_half", Res.drawable.red_apple_half)
}
