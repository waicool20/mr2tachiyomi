# MR2Tachiyomi

A simple tool that takes MangaRocks database and outputs a file that you can import using the backup restore tool in Tachiyomi. 

Only supports Android version of MangaRock.

Currently only supports the sources MangaRock, MangaEden, MangaReader.

## Usage

Currently the steps are very technical and might be complicated for the typical user, be aware!

Pre-requisites: 
 - [Java 8](https://www.oracle.com/java/technologies/jdk8-downloads.html)
 - [MR2Tachiyomi Application Jar](https://github.com/waicool20/mr2tachiyomi/releases)
 
## Method 1: Using mangarock.db (Rooted devices only)

1. Find and copy the `mangarock.db` file on your android device to your PC
    - File is located in the directory `root:/data/data/com.notabasement.mangarock.android.lotus/databases`  
    
2. Run MR2Tachiyomi Application Jar (Double click the jar) and open the copied `mangarock.db` file.  
Click `Run` and choose a save location for the output file.

Optionally for those wanting to use the command line instead of GUI: 
> java -jar mr2tachiyomi.jar -i /path/to/mangarock.db -o /path/to/output.json

3. After running there will be a new file created which can be imported into Tachiyomi 
using the backup restore feature in the settings menu. 
Note: MangaRock or other relevant Tachiyomi source extensions must be installed, or else the import will fail.

## Method 2: Using Helium Backup (Both Non-Rooted or Rooted devices)

1. Backup MangaRock using [Helium Backup](https://play.google.com/store/apps/details?id=com.koushikdutta.backup&hl=en)

2. Copy the file  
`sdcard:/carbon/com.notabasement.mangarock.android.lotus/com.notabasement.mangarock.android.lotus.ab`  
to your pc

3. Run MR2Tachiyomi Application Jar (Double click the jar) and open the copied `com.notabasement.mangarock.android.lotus.ab` file.  
Click `Run` and choose a save location for the output file. 

Optionally for those wanting to use the command line instead of GUI: 
> java -jar mr2tachiyomi.jar -i /path/to/androidbackup.ab -o /path/to/output.json

4. After running there will be a new file created which can be imported into Tachiyomi 
using the backup restore feature in the settings menu. 
Note: MangaRock or other relevant Tachiyomi source extensions must be installed, or else the import will fail. 

## TODO

- CSV output 
- Other sources that MangaRock uses

## Credits

- Original C implementation for Android Backup to Tar algorithm: https://github.com/floe/helium_ab2tar