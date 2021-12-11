# rxjava

Test code written to explore different parts of RxJava in a Mejsla study group.


## Mandelbrot fractals

A Swing application that draws Mandelbrot fractals. Demonstrates the use of *debounce* in connection with GUI events, see
[MandelController](https://github.com/dykstrom/rxjava/blob/master/src/main/java/se/dykstrom/rxjava/swing/mandel/MandelController.java).
Also demonstrates how to write an Observable that supports backpressure, see
[LineObservable](https://github.com/dykstrom/rxjava/blob/master/src/main/java/se/dykstrom/rxjava/swing/mandel/LineObservable.java).
