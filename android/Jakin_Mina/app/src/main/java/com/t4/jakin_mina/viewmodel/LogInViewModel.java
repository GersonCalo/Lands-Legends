/**
 * ViewModel para manejar la lógica de autenticación de usuarios:
 * - Comunicación con el repositorio para validar credenciales
 * - Gestión de estados (carga, éxito, errores)
 */
package com.t4.jakin_mina.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.t4.jakin_mina.model.UserRepository;

public class LogInViewModel extends AndroidViewModel {
    // LiveData para estados observables
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(); // Estado de carga
    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>(); // Resultado del login
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(); // Mensajes de error
    private final UserRepository userRepository = new UserRepository(); // Repositorio para operaciones de red

    /**
     * Constructor que recibe el contexto de la aplicación
     * @param application Contexto de la aplicación
     */
    public LogInViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Inicia el proceso de autenticación del usuario
     * @param email Correo del usuario
     * @param company Nombre de la empresa
     */
    public void loginUser(String email, String company) {
        isLoading.setValue(true); // Activar estado de carga
        userRepository.login(email, company, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(boolean userExists) {
                isLoading.postValue(false); // Desactivar carga
                loginResult.postValue(userExists); // Notificar resultado
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false); // Desactivar carga
                errorMessage.postValue(message); // Notificar error
            }
        });
    }

    // Getters para exponer LiveData a la UI
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getLoginResult() { return loginResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}