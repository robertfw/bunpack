# Bunpack

_Because I'm sick and tired of googling the args for unpacking things_

🐰
`bunpack` will extract whatever* you throw at it, extracting into a subfolder every time to prevent those annoying "I just spat junk all over my working directory" moments.

_*as long as it's a `tar.gz`, `tar.bz2`, `tar.xz`, or `zip`_

## Usage

`bunpack <file> [<destination>]`

Bunpack will select the correct decompression tool to use and ensure that your 
unpacked files will be in their own directory, because if there's one thing more
annoying that having to google the tar command args again, it's unpacking dozens
of files into your current directory.

Without passing `<destination>`, `bunpack` will create a target directory based
on the filename.

## Dependencies

You should have the following command line tools installed:
* `tar`
* `xz`
* `unzip`

## Installation

Get `bunpack.clj` somewhere on your `$PATH`. I would recommend cloning the 
repository and creating a link but whatever floats your boat.

## Contributing

PRs welcome! Please update tests if adding a new unpacker.

## License

MIT License

Copyright (c) 2021 Robert Warner

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
