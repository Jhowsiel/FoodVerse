package com.senac.food.verse;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.Date;

public class NtpTime {

    // Função que obtém a data do servidor NTP
    public static String pegarDataAtualNTP() {
        String servidorNTP = "pool.ntp.org"; // Servidor NTP público
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(10000); // Timeout de 10 segundos

        try {
            // Conectar ao servidor NTP
            InetAddress inetAddress = InetAddress.getByName(servidorNTP);
            TimeInfo info = client.getTime(inetAddress);
            
            // Obter o timestamp e convertê-lo para a data
            long time = info.getMessage().getTransmitTimeStamp().getTime();
            Date data = new Date(time);

            // Formatar a data para o padrão "dd/MM/yyyy"
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            client.close();
        }
    }
}
