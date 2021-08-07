# Tape Device Debugger

A GUI debugger for use with [Tape Device](https://github.com/raybritton/tape-device)

## Usage

Requires Java to be installed

### Build

`./gradlew gui:shadowJar`

### Run

`java -jar td_debug-1.2.0.jar`

To debug your app you have to generate debug info first via `tape-device assemble -d your_program.basm`

## Tape Device Compatibility

* `1.2.0` - Full compatibility
* `1.1.0` - All except no usage info on strings or data
* `1.0.0` - Incompatible as no debug data is generated

## Info

![Screenshot](screenshot.png)

#### Notes

 * Double click on a instruction to set/clear breakpoints
 * There's no value check and so it's possible to send invalid values and crash the tape device
 * This program uses stdin and stdout to communicate with an instance of tape device, sometimes they fill up and the program may freeze for a bit

## License 
```
MIT License

Copyright (c) 2021 Ray Britton

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```