# FORSE - Fast Overlay with Streaming Robust Evaluation

The FORSE (Fast Overlay with Robust Streaming Evaluation) engine incorporates a new approach to vector overlay.
It provides:

* *High Performance* - FORSE offers exceptional performance on all input datasets up to very large size.
* *Robust* - Several techniques are incorporated to provide 100% robust evaluation. These include exact evaluation of determinant sign, high-precision intersection computation, and snap-rounding.
* *Streaming Evaluation* - FORSE is able to operate on data streamed in from external memory. The resultant output is also streamed out. This means that very large datasets can be processed with only a small memory footprint. The only requirement is that the input data must be sorted along the X axis, which is easily accomplished.
* *Memory efficiency* - The FORSE internal memory structures are optimized for minimal size. Memory usage is sub-linear in the number of input line segments.
* *Flexible Input* - Any kind of polygonal data can be processed, including polygons with an arbitrary depth of overlap

The FORSE engine incorporates a flexible and powerful design which supports carrying out all of the following operations:

* Overlay of Polygons and Lines
* Union of Polygons and Lines
* Line segment noding
* Line segment dissolve
* Noding validation
* Polygonization
