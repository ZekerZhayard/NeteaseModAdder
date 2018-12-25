
:: Author  = ZekerZhayard
:: Version = 1.0

@CHCP 936 > NUL
@ECHO OFF
TITLE 一键删除模组 by ZekerZhayard
CD /D "%~dp0"

FOR /F "tokens=2,*" %%A IN (' REG QUERY "HKCU\Software\Netease\MCLauncher" /v "DownloadPath" ') DO SET "DP=%%B"
FOR /D %%A IN ("%DP%\cache\game\V_*") DO RD /S /Q "%%A\mods\"