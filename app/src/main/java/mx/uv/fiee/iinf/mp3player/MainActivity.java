package mx.uv.fiee.iinf.mp3player;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity {
    public static final int REQUEST_CODE = 1001;
    public static final int REQUEST_CODE_EXTERNAL_STORAGE = 1002;
    RecyclerView lv;
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        lv = findViewById (R.id.list);
        lv.setLayoutManager (new LinearLayoutManager (getBaseContext(), RecyclerView.VERTICAL, false));
        lv.addItemDecoration (new DividerItemDecoration (getBaseContext (), DividerItemDecoration.VERTICAL));
        // solicita el permiso necesario para leer del almacenamiento externo
        int perm = getBaseContext ().checkSelfPermission (Manifest.permission.READ_EXTERNAL_STORAGE);
        if (perm != PackageManager.PERMISSION_GRANTED) {
            requestPermissions (
                    new String [] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQUEST_CODE_EXTERNAL_STORAGE
            );
        } else {
            loadAudios ();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    void loadAudios () {
        String [] columns = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME};
        String order = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        Cursor cursor =  getBaseContext().getContentResolver().query (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, null, null, order);
        if (cursor == null) return;

        LinkedList<AudioModel> artists = new LinkedList<> ();

        for (int i = 0; i < cursor.getCount (); i++) {
            cursor.moveToPosition (i);
            AudioModel audioModel = new AudioModel ();
            int index = cursor.getColumnIndexOrThrow (MediaStore.Audio.Media._ID);
            audioModel.id = cursor.getLong (index);
            index = cursor.getColumnIndexOrThrow (MediaStore.Audio.Media.DISPLAY_NAME);
            audioModel.name = cursor.getString (index);
            artists.add (audioModel);
        }
        cursor.close ();
        MyAdapter adapter = new MyAdapter (getBaseContext (), artists);
        adapter.setOnAudioSelectedListener((item, name) -> {
            Bundle song = new Bundle();
            Intent intent = new Intent(getBaseContext(), DetailsActivity.class);
            song.putString("song", item.toString());
            song.putString("title", name);
            intent.putExtras(song);
            startActivity(intent);
        }
        );
        lv.setAdapter (adapter);
    }

    /**
     * Callback de la solicitud de permisos realizada en cualquier punto de la actividad.
     *
     * @param requestCode código de verificación de la solicitud
     * @param permissions conjunto de permisos solicitados
     * @param grantResults conjunto de resultados, permisos otorgados o denegados
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults [0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText (getBaseContext(),"Permission Granted!", Toast.LENGTH_LONG).show ();
                }
                break;
            case REQUEST_CODE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults [0] == PackageManager.PERMISSION_GRANTED) {
                    loadAudios ();
                }
        }
    }

    /**
     * Callback invocado después de llamar a startActivityForResult
     *
     * @param requestCode código de verificación de la llamadas al método
     * @param resultCode resultado: OK, CANCEL, etc.
     * @param data información resultante, si existe
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}

/**
 * Adaptador personalizado para controlar el llenado de datos del recyclerview
 */
class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private List<AudioModel> data;
    private OnAudioSelectedListener listener;

    public MyAdapter (Context context, List<AudioModel> data) {
        this.data = data;
        this.context = context;
    }

    /**
     * Manajador para el evento de selección de elemento en la lista
     * @param listener objeto que implementa la interfaz OnAudioSelectedListener
     */
    public void setOnAudioSelectedListener (OnAudioSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder (@NonNull MyViewHolder holder, int position) {
        String foo = data.get (position).name;
        holder.text1.setText (foo);

        holder.itemView.setOnClickListener (v -> {
            Uri contentUri = ContentUris.withAppendedId (
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    data.get (position).id
            );

            listener.audioSelected (contentUri, foo);
        });
    }

    @Override
    public int getItemCount () {
        return data.size ();
    }


    /**
     * Mantiene referencia al componente que interesa reutilizar en la vista
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text1;

        public MyViewHolder (@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById (R.id.tvItem);
        }
    }

}

/**
 * Interfaz que define al objeto manajador del evento click en algun elemento de la lista
 */
interface OnAudioSelectedListener {
    void audioSelected (Uri item, String title);
}

class AudioModel {
    long id;
    String name;

}