!include "Functions.nsh"

Name "Festc.exe"
OutFile "FESTC.exe"

Caption "FEST-C"

SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow

!define CLASSPATH "./bootstrap.jar;./lib/saf.core.runtime.jar;./lib/commons-logging.jar;./lib/jpf-boot.jar;./lib/jpf.jar;./lib/log4j-1.2.13.jar"
!define MAIN_CLASS "saf.core.runtime.Boot"

Section ""
  #Call GetJRE
  #Pop $R0

  StrCmp $R1 "" 0 +2
  StrCpy $R1 "${MAIN_CLASS}"
  
  ReadINIStr $1 .\festc.ini boot heapSize

  StrCpy $0 '"..\..\jre1.6.0\bin\javaw" -Xmx$1 -classpath "${CLASSPATH}" $R1'

  ;MessageBox MB_OK $0

  ClearErrors
  SetOutPath ".\plugins\bootstrap"
  
  ExecWait $0
  
  IfErrors 0 done
    MessageBox MB_OK "Could not find the java run time"
  done:
SectionEnd
