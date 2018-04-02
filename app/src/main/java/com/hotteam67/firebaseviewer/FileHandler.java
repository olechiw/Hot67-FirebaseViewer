package com.hotteam67.firebaseviewer;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;


/**
 * Created by Jakob on 3/26/2017.
 */

public final class FileHandler
{
    private static final String MATCHES_FILE = "viewerMatches.csv";
    private static final String TEAMS_FILE = "teamNames.json";
    private static final String RANKS_FILE = "teamRanks.json";
    private static final String DIRECTORY =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/BluetoothScouter/";

    public static final int VIEWER_MATCHES = 4;
    public static final int TEAM_NAMES = 5;
    public static final int TEAM_RANKS = 6;


    private static String file(int FILE)
    {
        String f = DIRECTORY;
        switch (FILE)
        {
            case VIEWER_MATCHES:
                f += MATCHES_FILE;
                break;
            case TEAM_NAMES:
                f += TEAMS_FILE;
                break;
            case TEAM_RANKS:
                f += RANKS_FILE;
                break;
            default:
                return null;
        }
        return f;
    }

    public static final BufferedReader GetReader(int FILE)
    {
        try
        {
            return  new BufferedReader(new FileReader(file(FILE)));
        }
        catch (FileNotFoundException e)
        {
            l("File not found: " + file(FILE));
            new File(DIRECTORY).mkdirs();

            try
            {
                new File(file(FILE)).createNewFile();
                return new BufferedReader(new FileReader(file(FILE)));
            }
            catch (Exception ex)
            {
                return null;
            }
        }
        catch (Exception e)
        {
            l("Exception occured in loading reader : " + e.getMessage());
        }

        return null;
    }

    public static final FileWriter GetWriter(int FILE)
    {
        String f = file(FILE);

        try
        {
            FileWriter writer = new FileWriter(new File(f).getAbsolutePath(), false);
            return writer;
        }
        catch (FileNotFoundException e)
        {
            l("File not found");
            new File(DIRECTORY).mkdirs();

            try
            {
                new File(f).createNewFile();
                return new FileWriter(new File(f).getAbsolutePath(), false);
            }
            catch (Exception ex)
            {
                return null;
            }
        }
        catch (Exception e)
        {
            l("Exception occured in loading reader : " + e.getMessage());
        }

        return null;
    }

    public static final String LoadContents(int file)
    {
        String content = "";
        BufferedReader r = GetReader(file);
        try
        {
            String line = r.readLine();
            while (line != null)
            {
                content += line + "\n";
                line = r.readLine();
            }
        }
        catch (Exception e)
        {
            l("Failed to load :" + e.getMessage());
        }

        return content;
    }

    public static final void Write(int FILE, String s)
    {
        try
        {
            FileWriter w = GetWriter(FILE);
            w.write(s);
            w.close();
        }
        catch (Exception e)
        {
            l("Failed to write: " + s + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static final void l(String s)
    {
        Log.d("[File Handling]", s);
    }
}
