;--------------------------------
; RepastSimphony.nsi
;
; This script builds the Repast Simphony installer/uninstaller
; Created by Jerry Vos, Nick Collier
; Adapted to FEST-C by IE, UNC
; Last modified Oct 29, 2009
;--------------------------------

;--------------------------------
; Header Files
!include "MUI.nsh"


;--------------------------------
; Variables

; Start menu dir var
Var "ICONS_GROUP"


;--------------------------------------------
; File macros
!define FILE_IGNORE_STRING "/x CVS /x .svn /x *.nsh /x *.res /x *.obj /x obj /x *.pch /x *.pdb /x debug /x *.ilk /x .cvsignore /x *.nsi /x distfiles /x distlib /x build_dist.xml /x config.properties"
!define File "File ${FILE_IGNORE_STRING} "
!define FileR "File /r ${FILE_IGNORE_STRING} "


;--------------------------------
; MUI Settings
!define MUI_ABORTWARNING
!define MUI_COMPONENTSPAGE_SMALLDESC

; if this is defined as a picture, it will be the picture that shows up on the header of the installer
;!define MUI_HEADERIMAGE

; Other settings
!define PRODUCT_NAME      "FESTC"
!define PRODUCT_VERSION    "1.0"
!define PRODUCT_PUBLISHER  "US EPA"
;!define PRODUCT_WEB_SITE   "http://www.epa.gov"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\FESTC"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"
!define PRODUCT_STARTMENU_REGVAL "NSIS:StartMenuDir"
!define USERHOME "$STARTMENU\..\festc"
!define licensefile "licenses\festc_gpl-3.0.txt"


;--------------------------------
; Custom defines

; This is the file that launches the main class.  This is created through RepastSimphonyLauncher.nsi
!define RS_LAUNCHER   "FESTC.exe"
; This is the location of the root of the sources.  This is used to find what files to copy.
!define SRCROOT        ".\"


;--------------------------------
; Setup the installer info

; The name of the installer
Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"


; This is the installer executable file's name
; File name has underscores only because sourceforge doesn't like spaces, can get rid of them if you don't want them
OutFile "${PRODUCT_NAME}_${PRODUCT_VERSION}_Installer.exe"

; The default installation directory
InstallDir "$PROGRAMFILES\${PRODUCT_NAME}_${PRODUCT_VERSION}"


; Registry key to check for directory (so if you install again, it will
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\${PRODUCT_NAME}" "Install_Dir"

ShowInstDetails show
ShowUnInstDetails show


;--------------------------------
; Installer pages

!insertmacro MUI_PAGE_WELCOME

; License page
!ifdef licensefile
LicenseText "FEST-C License"
LicenseData "${SRCROOT}\${licensefile}"
Page license
!endif
; End of license page

;!insertmacro MUI_PAGE_COMPONENTS

; Install directory selector
!define MUI_DIRECTORYPAGE_TEXT_TOP "Please select the directory to install ${PRODUCT_NAME}."
!define MUI_DIRECTORYPAGE_VARIABLE $INSTDIR
!insertmacro MUI_PAGE_DIRECTORY

; Start menu directory selector
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_DEFAULTFOLDER         "${PRODUCT_NAME}"
!define MUI_STARTMENUPAGE_REGISTRY_ROOT         "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_KEY          "${PRODUCT_UNINST_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME    "${PRODUCT_STARTMENU_REGVAL}"

!insertmacro MUI_PAGE_STARTMENU "StartMenuPageID" $ICONS_GROUP

; Install
!insertmacro MUI_PAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

;--------------------------------
; Uninstaller pages

!insertmacro MUI_UNPAGE_INSTFILES




;--------------------------------
; Install sections

Section "All" SecAll
  SectionIn 1

  ; Copy default festc properties file
  CreateDirectory "${USERHOME}"
  SetOutPath "${USERHOME}"
  File "${SRCROOT}\config.properties"
  
  ; BEGIN FILE LISTING
  ; Just copy everything
  SetOutPath "$INSTDIR"
  ${FileR} "${SRCROOT}\*"
  
  ; the RS launcher
  ${File} "${RS_LAUNCHER}"
  ; END FILE LISTING
  
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\${PRODUCT_NAME}.lnk" "$INSTDIR\${RS_LAUNCHER}"
SectionEnd


Section -Post
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  ;WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd


Section -AdditionalIcons
  !insertmacro MUI_STARTMENU_WRITE_BEGIN "StartMenuPageID"
    CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\"
    !ifdef PRODUCT_WEB_SITE
    WriteIniStr "$SMPROGRAMS\$ICONS_GROUP\${PRODUCT_NAME} Website.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
    !endif
    CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Uninstall.lnk" "$INSTDIR\uninstall.exe"
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd


;--------------------------------
; Uninstall sections
Section Uninstall

  !insertmacro MUI_STARTMENU_GETFOLDER "StartMenuPageID" $ICONS_GROUP

  Delete "$INSTDIR\uninstall.exe"

  RMDir /r "$SMPROGRAMS\$ICONS_GROUP"

  RMDir /r "$INSTDIR"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
;  SetAutoClose true
SectionEnd


;--------------------------------
; Functions

;Function Initialize
;  StrCpy $INSTDIR "$PROGRAMFILES\${PRODUCT_NAME}"
;FunctionEnd

Function un.onUninstSuccess
  MessageBox MB_ICONINFORMATION|MB_OK "${PRODUCT_NAME} was successfully removed from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove ${PRODUCT_NAME} and all of its components?" IDYES +2
  Abort
FunctionEnd
