# MR2Tachiyomi

A simple tool that takes MangaRocks database and outputs a file that you can import using the backup restore tool in Tachiyomi. 

Only supports Android version of MangaRock.

Currently only supports the sources MangaRock, MangaEden, MangaReader.

## Usage

Currently the steps are very technical and might be complicated for the typical user, be aware!

Pre-requisites: 
 - [Java 8](https://www.oracle.com/java/technologies/jdk8-downloads.html)
 - [MR2Tachiyomi Application Jar](https://github.com/waicool20/mr2tachiyomi/releases)
 - [Compiled Binary of Helium_AB2Tar](https://github.com/floe/helium_ab2tar) 

1. Find and copy the `mangarock.db` file on your android device to your PC
    - For rooted devices it is as simple as navigating to `root:/data/data/com.notabasement.mangarock.android.lotus/databases`  
    and copying the `mangarock.db` file to your pc
    
    - For non-rooted devices, the method of obtaining the `mangarock.db` will be more convoluted.
        1. Backup MangaRock using [Helium Backup](https://play.google.com/store/apps/details?id=com.koushikdutta.backup&hl=en)
        2. Copy the file  
        `sdcard:/carbon/com.notabasement.mangarock.android.lotus/com.notabasement.mangarock.android.lotus.ab`  
        to your pc
        3. Use Helium_AB2Tar tool to convert the ab file into a tar archive
        4. Open the archive using your favorite archive tool and navigate to `/apps/com.notabasement.mangarock.android.lotus/db/` and look for `mangarock.db`

2. Place the file `mangarock.db` in the same directory as the application jar on your PC and run it using java.
Double clicking the jar file or right clicking and choosing run with java should be enough to run it on windows, a simple GUI should pop up. Choose the button `Open` and select your `mangarock.db` file. Choose the `Run` button then choose a save location for the output file.

Optionally for those wanting to use the command line instead of GUI: 
> java -jar mr2tachiyomi.jar -i /path/to/mangarock.db -o /path/to/output.json

3. After running there will be a new file created which can be imported into Tachiyomi 
using the backup restore feature in the settings menu. Note: MangaRock or other relevant Tachiyomi source extensions must be installed, or else
the import will fail.

## TODO

- Might re-implement helium_ab2tar into mr2tachiyomi
- CSV output 
- Other sources that MangaRock uses
- Elaborate steps
