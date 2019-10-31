package mp.tareas.tarea5;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST = 200;
    public class ResultText
    {
        int inicio; int fin;
        public ResultText(int inicio, int fin)
        {
            this.inicio =inicio;
            this.fin = fin;
        }
    }
    File file;
    EditText editText;
    String fileName;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    ArrayList<ResultText> resultTexts = new ArrayList<>();
    SpannableString text;
    int current = -1;
    ImageView imgNext,imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String action = intent.getAction();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editText = findViewById(R.id.txt);
        imgBack = findViewById(R.id.img_back);
        imgNext = findViewById(R.id.img_next);

        imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current < resultTexts.size()-1) {
                    current++;
                    navigateText();
                }
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current > 0) {
                    current--;
                    navigateText();
                }

            }
        });

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileName == null)
                    showDialog();
                else
                    writeToFile();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.rl_res).setVisibility(View.VISIBLE);
                }
            });
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    findViewById(R.id.rl_res).setVisibility(View.GONE);
                    return false;
                }
            });
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i("onQueryTextChange", newText);
                    if (newText.length()>0)
                    //buscarTexto(newText);
                    search(editText.getText().toString(), newText);
                        else {
                        ((TextView)findViewById(R.id.txt_res)).setText("Resultados:");
                        editText.setText(editText.getText().toString());
                    }

                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i("onQueryTextSubmit", query);

                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        return true;
    }


    static int NO_OF_CHARS = 256;

    //A utility function to get maximum of two integers
    static int max (int a, int b) { return (a > b)? a: b; }

    //The preprocessing function for Boyer Moore's
    //bad character heuristic
    static void badCharHeuristic( String str, int size,int badchar[])
    {
        int i;

        // Initialize all occurrences as -1
        for (i = 0; i < NO_OF_CHARS; i++)
            badchar[i] = -1;

        // Fill the actual value of last occurrence
        // of a character
        for (i = 0; i < size; i++)
            badchar[(int) str.charAt(i)] = i;
    }

    /* A pattern searching function that uses Bad
    Character Heuristic of Boyer Moore Algorithm */
    void search( String txt,  String pat)
    {
        txt = txt + " ";
        resultTexts.clear();
        text = new SpannableString(editText.getText().toString());

        int m = pat.length();
        int n = txt.length();

        int badchar[] = new int[NO_OF_CHARS];

      /* Fill the bad character array by calling
         the preprocessing function badCharHeuristic()
         for given pattern */
        badCharHeuristic(pat, m, badchar);

        int s = 0;  // s is shift of the pattern with
        // respect to text
        while(s <= (n - m))
        {
            int j = m-1;

          /* Keep reducing index j of pattern while
             characters of pattern and text are
             matching at this shift s */
            while(j >= 0 && pat.charAt(j) == txt.charAt(s+j))
                j--;

          /* If the pattern is present at current
             shift, then index j will become -1 after
             the above loop */
            if (j < 0)
            {
                System.out.println("Patterns occur at shift = " + s);
                resultTexts.add(new ResultText(s, s + ( (s+m < n)? m-badchar[txt.charAt(s+m)]  -1: 0)) );
                text.setSpan(new ForegroundColorSpan(Color.BLUE), s, s + ( (s+m < n)? m-badchar[txt.charAt(s+m)] -1 : 0), 0);


              /* Shift the pattern so that the next
                 character in text aligns with the last
                 occurrence of it in pattern.
                 The condition s+m < n is necessary for
                 the case when pattern occurs at the end
                 of text */
                s += (s+m < n)? m-badchar[txt.charAt(s+m)] : 1;

            }

            else
              /* Shift the pattern so that the bad character
                 in text aligns with the last occurrence of
                 it in pattern. The max function is used to
                 make sure that we get a positive shift.
                 We may get a negative shift if the last
                 occurrence  of bad character in pattern
                 is on the right side of the current
                 character. */
                s += max(1, j - badchar[txt.charAt(s+j)]);
        }

        ((TextView)findViewById(R.id.txt_res)).setText("Resultados: " + resultTexts.size());
        editText.setText(text);

    }

    private void buscarTexto(String busqueda) {
        int i =0;
        resultTexts.clear();
        current = -1;
        text = new SpannableString(editText.getText().toString());
        while (i< editText.getText().toString().length() && i + busqueda.length() <= editText.getText().toString().length())
        {

            String txtTemp = text.subSequence(i, i + busqueda.length()).toString();
            if (txtTemp.equals(busqueda))
            {
                resultTexts.add(new ResultText(i, i + busqueda.length()));
                text.setSpan(new ForegroundColorSpan(Color.BLUE), i, i + busqueda.length(), 0);
                i= i + busqueda.length();
            }
            else
            i++;
        }
        ((TextView)findViewById(R.id.txt_res)).setText("Resultados: " + resultTexts.size());
        editText.setText(text);
    }

    void navigateText()
    {
        if (current>=0) {
        SpannableString tempString = text;
        for (ResultText res:resultTexts) {
            tempString.setSpan(new ForegroundColorSpan(Color.BLUE), res.inicio, res.fin, 0);
        }
            tempString.setSpan(new ForegroundColorSpan(Color.GREEN), resultTexts.get(current).inicio, resultTexts.get(current).fin, 0);
            editText.setText(tempString);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            Intent intent = new Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Selecciona un archivo de texto"), 123);
            return true;
        }
        if (id == R.id.action_save) {
            if (fileName == null)
                showDialog();
            else
                writeToFile();
            return true;
        }
        if (id == R.id.action_save_as) {
            showDialog();
            return true;
        }
        searchView.setOnQueryTextListener(queryTextListener);

        return super.onOptionsItemSelected(item);
    }

    private void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guardar archivo como:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileName = input.getText().toString() + ".txt";
                writeToFile();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            fileName =getFileName(selectedfile);
            open();
        }
    }

    boolean isExternalStorageAvailable()
    {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeToFile();
                } else {
                }
                return;
            }
        }
    }

    private void open()
    {

        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
        }
        editText.setText(text.toString());
    }

    private void writeToFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            }
        } else {
            if (isExternalStorageAvailable()) {
                FileOutputStream fos = null;
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                try {
                    fos = new FileOutputStream(file);
                    fos.write(editText.getText().toString().getBytes());
                    fos.close();
                    Toast.makeText(this, "Archivo guardado correctamente", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

}
