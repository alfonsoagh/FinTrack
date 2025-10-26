package com.pascm.fintrack.ui.grupo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.GroupEntity;
import com.pascm.fintrack.data.repository.GroupRepository;
import com.pascm.fintrack.util.SessionManager;

public class CrearGrupoFragment extends Fragment {

    private TextInputEditText edtNombreGrupo;
    private TextInputEditText edtDescripcion;
    private MaterialButton btnCrearGrupo;
    private GroupRepository groupRepository;
    private long userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear_grupo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository and user ID
        groupRepository = new GroupRepository(requireContext());
        userId = SessionManager.getUserId(requireContext());

        initViews(view);
        setupListeners(view);
    }

    private void initViews(View view) {
        edtNombreGrupo = view.findViewById(R.id.edt_nombre_grupo);
        edtDescripcion = view.findViewById(R.id.edt_descripcion);
        btnCrearGrupo = view.findViewById(R.id.btn_crear_grupo);
    }

    private void setupListeners(View view) {
        // Botón atrás
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        // Botón cancelar
        view.findViewById(R.id.btn_cancelar).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        // Botón crear grupo
        btnCrearGrupo.setOnClickListener(v -> crearGrupo(view));
    }

    private void crearGrupo(View view) {
        String nombreGrupo = edtNombreGrupo.getText() != null ? edtNombreGrupo.getText().toString().trim() : "";
        String descripcion = edtDescripcion.getText() != null ? edtDescripcion.getText().toString().trim() : "";

        if (nombreGrupo.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa un nombre para el grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double-click
        btnCrearGrupo.setEnabled(false);

        // Create group entity
        GroupEntity group = new GroupEntity();
        group.setGroupName(nombreGrupo);
        group.setDescription(descripcion.isEmpty() ? null : descripcion);
        group.setAdminUserId(userId);

        // Create group in database
        groupRepository.createGroup(group, groupId -> {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Grupo creado exitosamente", Toast.LENGTH_SHORT).show();

                // Navigate to members screen
                Bundle args = new Bundle();
                args.putLong("groupId", groupId);
                Navigation.findNavController(view).navigate(R.id.action_crearGrupo_to_miembrosGrupo, args);
            });
        });
    }
}
