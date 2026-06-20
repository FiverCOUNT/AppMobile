package com.factapp.jhonny.network

/**
 * URL base del API BackEndEasy (Node en el escritorio).
 *
 * - Emulador: `10.0.2.2` apunta al `localhost` de tu PC.
 * - Teléfono físico: cambia por la IP LAN de tu PC, ej. `http://192.168.1.50:3000/api/`
 *
 * Debe terminar en `/` para que Retrofit concatene rutas como `auth/login`.
 */
object ApiConfig {
    /**
     * Teléfono físico + `adb reverse tcp:3000 tcp:3000` → PC localhost.
     * Emulador: usar `http://10.0.2.2:3000/api/`
     * Red LAN: `http://IP_DE_TU_PC:3000/api/`
     */
    const val BASE_URL: String = "http://192.168.18.69:3000/api/"
}
