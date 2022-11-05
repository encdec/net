Desktop

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.encdec/eddsk/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.encdec/eddsk)

Command Line

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.encdec/edcmd/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.encdec/edcmd)

Thank you for using EncDec!  
This README file explains how to use the EncDec executable program from EncDec, LLC, 
available for download on the Internet at https://www.encdec.net

ABOUT  
This EncDec encryption tool is a java software application. It uses methods of 
advanced encryption standard (AES), initialization vector (IV), and cipher block 
chaining (CBC) to create a secure encryption output of a given file or text source. 
Review the source code included to examine these methods closer in their usage.

DEPENDENCIES  
Operations System (OS):
Windows (XP, 7, Vista, 8, or 10)
Application Programs:
Windows Powershell 2.0
Java 7 or higher

PROGRAM STARTUP  
On startup, the EncDec program will create five folders onto your file system in the 
Documents tree: 'EncDec Files', 'Encrypted', 'Decrypted', 'Downloads', and 'Receipts'. 
The EncDec Files folder is the root folder of the program. The Encrypted and Decrypted 
folders are the subfolders that will store your created files. The Downloads folder is 
for retrieving inbox and outbox files (See Registration Section). The Receipts folder 
is for downloading your receipts after registration purchases. EncDec also creates a 
configuration folder, 'edconfig', in your default My Documents file structure. Do not 
alter this folder as you may corrupt you desktop program settings.

---- GUI ----  
Executable Programs: EncDec.exe or eddsk-{version}.jar

SET MODE  
Set the mode you desire using the Switch Mode button (Encrypt or Decrypt, defaults 
to Encrypt on startup). Upload a file or folder you want to process in the first textbox using 
the Upload button. The upload textbox is disabled to prevent changes to the file 
location obtained through your OS system through the upload button.

ENCRYPT MODE  
Enter a passcode string of at least 8 alphanumeric chars to encrypt the file. Some 
special chars are accepted (!@#$%^&*_=+?/,.;:-). DO NOT lose or forget your passcode. 
If the new encrypted file is successfully created, the original passcode will be 
needed to decrypt it. Otherwise, the encrypted file(s) will be irrecoverable. Optionally, 
you can give the new encrypted file a new file name. If not, the file name will 
default to the name 'file'. Click the Encrypt File button. If successful, the program 
will produce a success message in green color at the bottom of the panel. It will also 
print the full location of the new file(s) in the bottom disabled textbox. You may copy this 
location and paste it into a file explorer window to retrieve your new file output.

DECRYPT MODE  
Enter the known alphanumeric passcode that was used to encrypt the file(s). The New 
File Name textbox is disabled in decrypt mode because it is not needed. Click the 
Encrypt File button. If successful, the program will produce a success message in 
green color at the bottom of the panel. It will also print the full location of the 
new file(s) in the bottom disabled textbox. You may copy this location and paste it 
into a file explorer window to retrieve your new file output.  
---- END GUI ----  

---- COMMAND LINE ----  
If running the edcmd-{version}.jar file from the command line, below are arguments for EncDec to 
run successfully.

Syntax  
$DRIVE: java -jar /path/to/jar/file/edcmd-{version}.jar -option argument

Options  
--help  
Provides a welcome message, basic instructions, and a list options with argument examples.  
No argument needed  

-file  
File path argument for the program to perform an encryption/ task  
Required, use quotes if path contains spaces  
Ex. -file "/full/path/to/file/or/folder"  

-passocde  
Passcode used for the encryption/ algorithm  
Required, at least 8 chars, special chars limited (!@#$%^&*_=+?/,.;:-)  
Ex. -passcode abcd1234  

-name  
New file name of a processed file in Encrypt Mode  
Not required. For Encrypt Mode only, default name='file' if not set, use quotes if name contains spaces  
Ex. -name "My New File" **no extention**  

-mode  
Mode selection to perform an encryption/ task  
Required, only 2 inputs (enc for Encryption, dec for Decryption)  
Ex. -mode enc  

-out  
New output folder location for a successfully processed file in either mode  
Not required. If not set, program will place processed files in the default locations:  
$DRIVE:Documents/EncDec Files/Encrypted/, $DRIVE:Documents/EncDec Files/Decrypted/  
Ex. -out /path/to/desired/folder/  

-noprint  
Set program to print output to console  
Not required. no inputs needed  
If not used, program is set to print all output to console  
Ex. -noprint  
---- END COMMAND LINE ----  

---- JAVA DEPENDENCY ----  
To use as a java dependency, load the edcmd dependency .jar into your java development 
environment as an maven dependency or external jar resource. Next, create or go to your
desired class file and import the EncDec class from the code package 
(Ex. import net.encdec.edcmd.code.EncDec;). Call the 'process' method from the 
EncDec class and pass it the correct arguments.

Example:  
EncDec ed = new EncDec();  
ed.process(processFile, pw, newFn, outDir, eMode, pnt);  

A description of the require fields:  
* @param processFile The file or folder to process  
* @param pw User created secret passcode  
* @param newFn An optional new file name (encrypt mode only)  
* @param outDir The output folder where the new processed file will be saved  
* @param eMode The encryption mode (true for encryption, false for decryption)  
* @param pnt Console output switch (true for print, false for no print)  

---- END JAVA DEPENDENCY----  