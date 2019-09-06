# MR2Tachiyomi

A simple tool that takes MangaRocks database and outputs a file that you can import using the backup restore tool in Tachiyomi. 

Only supports Android version of MangaRock.

Currently only supports the sources MangaRock, MangaEden, MangaReader.

## Usage

1. Find and copy the mangarock.db file on your android device
    - For rooted devices it is as simple as navigating to `/data/data/com.notabasement.mangarock.android.lotus/databases` and
    copying the `mangarock.db` to your pc
    
    - TODO: Add steps for non-rooted devices

2. Place the file `mangarock.db` in the same directory as the application jar and run it using java

3. After running there should be a file called `output.json` which can be imported into Tachiyomi 
using the backup restore feature in the settings menu.

## TODO

- CSV output 
- GUI?
- Other sources that MangaRock uses
