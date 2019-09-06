# MR2Tachiyomi

A simple tool that takes MangaRocks database and outputs a file that you can import using the backup restore tool in Tachiyomi. 

Only supports Android version of MangaRock.

Currently only supports the sources MangaRock, MangaEden, MangaReader.

## Usage

1. Find and copy the mangarock.db file on your android device
    - For rooted devices it is as simple as navigating to `root:/data/data/com.notabasement.mangarock.android.lotus/databases` and
    copying the `mangarock.db` to your pc
    
    - For non-rooted devices, the method of obtaining the `mangarock.db` will be more convoluted.
        1. Backup MangaRock using [Helium Backup](https://play.google.com/store/apps/details?id=com.koushikdutta.backup&hl=en)
        2. Copy the file `sdcard:/carbon/com.notabasement.mangarock.android.lotus/com.notabasement.mangarock.android.lotus.ab` to your pc
        3. [Use this tool to convert it into a regular tar archive file](https://github.com/floe/helium_ab2tar)
        4. Open the archive and navigate to `/apps/com.notabasement.mangarock.android.lotus/db/` and look for `mangarock.db`

2. Place the file `mangarock.db` in the same directory as the application jar and run it using java

3. After running there should be a file called `output.json` which can be imported into Tachiyomi 
using the backup restore feature in the settings menu. Note: MangaRock or other relevant tachiyomi source extensions must be installed, or else
the import will fail.

## TODO

- CSV output 
- GUI?
- Other sources that MangaRock uses
- Elaborate steps
