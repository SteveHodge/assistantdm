Attribute VB_Name = "DMAssistant"
'Export for HeroForge 6.0.1
Sub ExportXML()
    'vvv this code from ImportExport module vvv
    ' Windows or Mac?
    ' These file-type filters could use to be refined
    Dim osType As String
    Dim fileToSave As Variant
    Dim initialFileToSave As String
    Dim fileExists As Integer
    fileExists = vbCancel

    Dim fs As Object
    Set fs = CreateObject("Scripting.FileSystemObject")

    osType = Application.OperatingSystem
     initialFileToSave = Range("CharacterName").Value & " (Lvl " & Range("ClassLvl").Value & ")_" & Range("VersionNumber").Value & ".xml"
    
    Do
        If osType Like "*Windows*" Then
            fileToSave = Application.GetSaveAsFilename( _
                InitialFilename:=initialFileToSave, _
                fileFilter:="XML Files (*.xml), *.xml")
        Else
             fileToSave = Application.GetSaveAsFilename( _
                 InitialFilename:=initialFileToSave)
        End If

        'If they cancelled, don't do anything
        If osType Like "*Windows*" Then
            If fileToSave <> False Then
                If fs.fileExists(fileToSave) Then
                    fileExists = MsgBox("Warning: " & Chr(13) & _
                        "File exists, overwrite? ", _
                        vbYesNoCancel, "HeroForge Export")
                    If fileExists = vbYes Then
                        fs.DeleteFile (fileToSave)
                    End If
                Else
                    fileExists = vbYes
                End If
            End If
        Else
            fileExists = vbYes
        End If
    Loop Until fileExists <> vbNo
        
    If fileExists = vbCancel Then
        Exit Sub
    End If
    '^^^ this code from ImportExport module ^^^^

    Dim f
    Set f = fs.CreateTextFile(CStr(fileToSave), True)
    
    f.WriteLine ("<?xml version=""1.0"" encoding=""UTF-8""?>")
    f.WriteLine ("")
    f.WriteLine ("<Party xmlns:xsi=""http://www.w3.org/2001/XMLSchema-instance"" xsi:noNamespaceSchemaLocation=""party.xsd"">")
    f.WriteLine (vbTab & "<Character name=""" & Replace(Range("CSCharacterName").Value, """", "&quot;") & """>")

    'Abilities
    f.WriteLine (vbTab & vbTab & "<AbilityScores>")
    f.WriteLine (vbTab & vbTab & vbTab & "<AbilityScore type=""Strength"" value=""" & Range("CSStr").Value & """/>")
    f.WriteLine (vbTab & vbTab & vbTab & "<AbilityScore type=""Dexterity"" value=""" & Range("CSDex").Value & """/>")
    f.WriteLine (vbTab & vbTab & vbTab & "<AbilityScore type=""Constitution"" value=""" & Range("CSCon").Value & """/>")
    f.WriteLine (vbTab & vbTab & vbTab & "<AbilityScore type=""Intelligence"" value=""" & Range("CSInt").Value & """/>")
    f.WriteLine (vbTab & vbTab & vbTab & "<AbilityScore type=""Wisdom"" value=""" & Range("CSWis").Value & """/>")
    f.WriteLine (vbTab & vbTab & vbTab & "<AbilityScore type=""Charisma"" value=""" & Range("CSCha").Value & """/>")
    f.WriteLine (vbTab & vbTab & "</AbilityScores>")

    'Hitpoints
    f.WriteLine (vbTab & vbTab & "<HitPoints maximum=""" & Range("CSHP").Value & """/>")
        
    'Initiative
    f.WriteLine (vbTab & vbTab & "<Initiative value=""" & Range("Misc.InitiativeMod") & """/>")

    'Saving Throws
    f.WriteLine (vbTab & vbTab & "<SavingThrows>")
    f.WriteLine (vbTab & vbTab & vbTab & "<Save type=""Fortitude"" value=""" & (Range("CSFortSave").Value + Range("Misc.FortMod").Value) & """/>")
    f.WriteLine (vbTab & vbTab & vbTab & "<Save type=""Reflex"" value=""" & (Range("CSRefSave").Value + Range("Misc.ReflexMod").Value) & """/>")
    f.WriteLine (vbTab & vbTab & vbTab & "<Save type=""Will"" value=""" & (Range("CSWillSave").Value + Range("Misc.WillMod").Value) & """/>")
    f.WriteLine (vbTab & vbTab & "</SavingThrows>")

    'Skills
    f.WriteLine (vbTab & vbTab & "<Skills>")
    For x = 1 To 57
        Dim str As String
        str = Range("CSSkillName" & x).Value
        If Right(str, 1) = "¹" Then
            str = Left(str, Len(str) - 1)
        End If
        Dim ranks
        ranks = Range("CSSkillRanks" & x).Value
        Dim misc
        misc = Range("CSSkillMiscMods" & x).Value
        If Len(str) > 0 And (ranks <> 0 Or misc <> 0) Then
            f.Write (vbTab & vbTab & vbTab & "<Skill type=""" & str & """ ranks=""" & ranks)
            If (misc <> 0) Then
                f.Write (""" misc=""" & misc)
            End If
            f.WriteLine ("""/>")
        End If
    Next x
    f.WriteLine (vbTab & vbTab & "</Skills>")

    'AC
    f.WriteLine (vbTab & vbTab & "<AC>")
    If Range("ACArmor").Value <> 0 Then
        f.WriteLine (vbTab & vbTab & vbTab & "<ACComponent type=""Armor"" value=""" & Range("ACArmor") & """/>")
    End If
    If Range("ACShield").Value <> 0 Then
        f.WriteLine (vbTab & vbTab & vbTab & "<ACComponent type=""Shield"" value=""" & Range("ACShield") & """/>")
    End If
    If Range("ACDex").Value <> 0 Then
        f.WriteLine (vbTab & vbTab & vbTab & "<ACComponent type=""Dex"" value=""" & Range("ACDex") & """/>")
    End If
    If Range("ACSize").Value <> 0 Then
        f.WriteLine (vbTab & vbTab & vbTab & "<ACComponent type=""Size"" value=""" & Range("ACSize") & """/>")
    End If
    If Range("NaturalArmor").Value <> 0 Then
        f.WriteLine (vbTab & vbTab & vbTab & "<ACComponent type=""NaturalArmor"" value=""" & Range("NaturalArmor") & """/>")
    End If
    If Range("ACDeflect").Value <> 0 Then
        f.WriteLine (vbTab & vbTab & vbTab & "<ACComponent type=""Deflect"" value=""" & Range("ACDeflect") & """/>")
    End If
    If Range("ACMisc").Value <> 0 Then
        f.WriteLine (vbTab & vbTab & vbTab & "<ACComponent type=""Misc"" value=""" & Range("ACMisc") & """/>")
    End If
    f.WriteLine (vbTab & vbTab & "</AC>")

    f.WriteLine (vbTab & "</Character>")
    f.WriteLine ("</Party>")
    f.Close
End Sub
