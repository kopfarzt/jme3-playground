# Texture

## Notes

* Open with IntelliJ from Advent of Code 2024
* New class `ParameterizedSurfaceGrid`
    * Computes normals directly at vertices by creating four points in the very vicinity of the vertex and computing the
      cross product of the diagonal vectors between these 4 points. Surfaces can define a minimum and a maximum value.
      In this case, the points used for computing the normal will always be computed to be inside the min/max boundary.
    * Removes degenerated triangles. Degenerated triangles are found, when two points of the triangle are very close
      together.

### PBR

* Metallic 0: Plastic, 1: Metal >1: Black Metal
* BaseColor is used

## TODO

* Move Buffer Logging to utils project
* Check index buffer (why are there in a 4x5 surface 4 triangles with 0 0 0??)
* For all inherited `ParameterizedSurface`
    * use `FastMath` and `float` where possible
    * check min max

## DONE

* Fixed some other problems caused by degenerated triangles which were not correctly detected and removed.
* In `TextureTest` spheres have texture problems at the poles. Reason is most likely, that the squares on poles collapse
  to two triangles.
    * The computed coordinates for the pole quads should be checked.
    * Perhpaps a logic can be found to eliminate the duplicates.
    * ~~Currently, a small random offset is added to the computed coordinates.~~
    * Note: a sphere with 3x3 segments produces 16 vertices. For a parameterized sphere, this means, that the equatorial
      points overlap each other. If possible, these should be removed by a cleanup step or not produced for closed
      objects.
    * In `SmoothParameterizedSurfaceGrid` a few functions are prepared to merge point within a limit. Look at
      `SubBlock` and `BlockMap`
    * New class `ParameterizedSurfaceGrid` only computes within the bounds and estimates normals by looking into the
      very next vicinity of points. The vicinity can be bound by the geometry to avoid overshooting into undefined or
      problematic areas.

# SpringTriangulation

## TODO

* Tetraeder Physics gets chaotic for depth > 1 or higher gravitation
* When started, the Tetraeder starts to move up instead of hovering still in the air
* The motion never stops event though I built in a threshold into the Spring Controller
* The objects are not on the ground plane, but always stay a small size above

## DONE
