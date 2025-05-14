package com.superwatch.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class FormHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String htmlForm = """
<!DOCTYPE html>
<html lang='fr'>
<head>
    <meta charset='UTF-8'>
    <meta name='viewport' content='width=device-width, initial-scale=1.0'>
    <title>SuperWatch</title>
    <style>
        * {
            padding: 0;
            margin: 0;
            box-sizing: content-box;
            font-size: 20px;
            color: white;

            -webkit-user-select: none;
            -webkit-touch-callout: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
        }

        html {
            background: rgb(2, 0, 36);
            background: linear-gradient(180deg,
                rgba(2, 0, 36, 1) 0%,
                rgba(7, 7, 120, 1) 35%,
                rgba(2, 0, 36, 1) 100%);
            min-height: 100%;
            font-family: Arial, Helvetica, sans-serif;
        }

        body {
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        .container {
            text-align: center;
            padding: 40px;
            border-radius: 16px;
        }

        h1 {
            color: white;
            font-size: xx-large;
            margin-bottom: 50px;
        }

        .buttons {
            display: flex;
            justify-content: center;
        }

        .buttons form {
            display: flex;
            flex-direction: row;
            gap: 40px;
            padding: 20px;
        }

        .button {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            width: 180px;
            height: 180px;
            font-size: 18px;
            font-weight: bold;
            cursor: pointer;
            border-radius: 12px;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            border: 2px solid;
            padding: 20px;
            background-color: rgba(0, 120, 215, 0.7);
            color: white;
            border-color: #0078d7;
            margin: 0;
        }

        .button:hover {
            transform: scale(1.1);
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
        }

        .logo {
            font-size: 64px;
            margin-bottom: 15px;
        }

        .button-text {
            margin-top: 10px;
        }

        .logo {
            width: 80px;
            height: 80px;
            margin-bottom: 10px;
            background-repeat: no-repeat;
            background-position: center;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .image-windows-logo, .image-linux-logo {
            height: 100%;
        }
    </style>
    <style>
            .hidden {
            opacity: 0;
            visibility: hidden;
            transition: all 0.5s ease;
        }

        .loading-screen {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: rgba(0, 0, 0, 0.9);
            border-radius: 16px;
            opacity: 0;
            visibility: hidden;
            transition: all 0.5s ease 0.3s;
        }

        .loading-screen.active {
            opacity: 1;
            visibility: visible;
        }

        .spinner {
            width: 50px;
            height: 50px;
            border: 4px solid #0078d7;
            border-top: 4px solid transparent;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin-bottom: 20px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .loading-text {
            font-size: 24px;
            color: #0078d7;
            text-transform: uppercase;
            letter-spacing: 2px;
        }
    </style>
</head>
<body>
    <div class='container'>
        <h1>Choisissez votre système d'exploitation</h1>
        <div class='buttons'>
            <form action='/install' method='POST' id="osForm">
                <button type='submit' name='os' value='windows' class='button'>
                    <div class='logo'><img class='image-windows-logo' src='data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAgAAAAIACAMAAADDpiTIAAAAA3NCSVQICAjb4U/gAAAACXBIWXMAAA+lAAAPpQGN0OmBAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAwBQTFRF////AQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACAQACl5/7/AAAAP90Uk5TAAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+6wjZNQAACZpJREFUeNrt3Olu1OcZxuF3vEFIwr6DIUAgrF7m/h9HP/Rrj6SqWqlSpR5CVfUQqi5JyApZyAokOYW2Wds03dOmTcLSD6YBGgzGMWZmnusnSw4Ye2be+8rMeCyYaBrxpk4l+cWPF/nohAMa5e1nkpycaq2dbgDUas2ppDs5ecc/B8AIbj+TZAnbAzCK23fJicm7+AwARqW1M7nL7QEYme1nk5xY3pQA1N0egOHugdkkx7/pgAAM6fZdjq3IdgAM2/ZzSY6Pr9jXA2B4WjeX5Nj4yn5RAOpuD8BQ9OBckqPj9+rLAzDo2x8bu6eXAcCAbj+f5OjYvb8gAAauh+bS5bGxVbo0AAZq+/lk9bYHYJB6eD7JkbFVv1wA6m4PwABs309ypHcfrwEA96v180l3uHe/rwYA92P7fpL7vz0A96EN813yaG9wrhAAq7d9P4O1PQCr1sZ+kkO9QbxqANTdHoB73KZ+kkMDfiUBuGfbdweH4ZoCsOLbJ8nBobm6AKxgm/tdcmC4rjMAK7R9MnTbA7AybUmSR4b12gNQd3sAvklbk2T/8N8OAJa1fZd9I3JjALirtiUZme0BuKu297tkeuRuFgBL2T4Zxe0BWEI7kmTvCN9AAOpuD8Ci7UySPSVuKgBf377L7jq3F4Dr7UpSaXsAbti+S7Kr4i0HYHeqbg/AniTZWdv/hO0BKNbeJNlh+ooA9qZLtlu9IoDpxPZFAUx3SbbZuiSAqR/YvjSAdd+zsecAAkAACAABIAA0/ACmk1z6lvkqAvjqdb0L1qsGYF+8plsVwP4k2WqvigD2p0vf9iUBPJIkW6xUEcCBJH3blwRwIOn6m21TEcDBJNlklooADiXp274igN7BJNlojIIAeoeS9G1fEUDv0ST9DSYoCMD2dQH0Difpr3fwBQH0DnfJvO0rAugdSdJ/2HEXBDB2JMm87SsCGDuSLvMPOeSCAMYeS2xfE8DY0SRztq8IYOxo0s096FQLAhg/msT2JQGMH0syt85ZFgRg+7oAxo8nmXvACRYEMHE8yaztKwKwfV0AEyeSzK51ZAUBTJzokhnbVwQweSK2Lwpg8mSSmTXOqCaAb3/3lO0rA5jpHI7nAAJAAAgAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAAQAAJAAAgAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAAQAAJAAAgAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAAQAAJAAAgAAQCAABAAAkAACAABIAAEgAAQAAJAAAgAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAAQAAJAAAgAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAAQAAJAAAgAASAA1Fprly59eYe38wCMwIr/9/bVf15yDzBAK95yoqW8eQgY0BXvalHPAQasy79Z+j3p8K44ogCufnnj/4pfe/feUr7Gp4fcod1fALfcbrFNb353xTwDAuDyEtZa5PcvO+IhBfCTn3+14qWrzqkegI8/djgeAgSAABAAAkAACAABIAAEgAAQAAJAAAgAASAABIAAEAACQAAIAAEgADR8AKb33fRXpv0dv2oAvvPjm3559dIy/90LdkbjIaA3Obnci2Cn+HOAe2Xndz8z36g/CbytnQtLAbD+tzfzucU/DXPL91cBGI3GNi/v867c1sjtPnQJgJGAs2bNcj/1zkYW+9AVAEbiTnXZZ3rljo9Mi34IgJG425maWvbdzh3YvHYegNJ3Oz8EQAAIAAEgAASAABAAAgAAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAAQAAJAAAgAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAAQAAJAAAgAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAAQAAJAAAgAASAABIAAEAACQAAIAAEgAASAABAAAAgAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAAQAAJAAAgAASAABIAAEAACQAAIAAEgAASAABAAAkAACAABIAAEgAYOwDvPZ4vjKQzgzJm2P136W51R2YeAd9/9ZWv7k4SCus8BKPAk8JqCfekoqPxdwHvv/aq1fUmyzbmV/TaQAq8DXFMwnXQU1ATQWmvt/fd/3dp0kmx3jBUBUADAjQr2Jh0FNQG01lr74IPHW9ubJDucakUA1xXs6SioCqC11tqHHz7e2p6ko6AmgGsKnmhtT5LsdMgVAVxXsLujoCqA1lprHz3xRGu7k46CmgAWFHz0ZGu7k2SXM68I4LqCXR0FVQG01lr7/ZNPtrYrSUdBSQALCk6fXlCQ3SaoCOC6gp0dBVUBtNZa+8Pp063tTNJRUBLAgoKnnlpQkD0WqQjguoId6SgoCqC11trHTz/d2o4kHQUlAdysIHsNVBHAdQXb01FQFEBrrbU/PvNMa9uTZNpUFQFQAMCNCralo6AogNZaa588+2xr25Jkn+UqArhZwWXrlQRwXYHqAhAAAkAACAABIAAW6bPve+2wNIAvfuTnCOUfAq79HCEdBYWfA/g5gieB/1PQ7yio/F3AJ88919rWJNlv/7LfBv6JAq8DLCjYkqTbD0DZ/vz88wsK8ggAxRX005VTAMANCs6caW1zkhwAoGx/OXOmtc39rooCAG6p4OzZ1jb3kxwEoLaCTRlxBQDcvr+ePdvapn7SHQSgroIXXlhQkEMA1FawsZ8uB3sAVO1vL77Y2sZ+kkM9AEor2NDvRkIBAMvs7y+91NqGfpJHewBQMLQKAFgZBev7STeMCgBYmf7x8ssLCnK4B0BtBfPphkgBACuu4Ny51tbPJznSA6C0gof7Q6AAgHvXp+fOtfbwfJIjYwCUVfDKKwOtAIBVU/DQfNINnAIAVq1/vvrqgoI8NgZAbQVz3cAoAOB+KHjttdYenE9ydAyAqv1rMBQAMAAK5pIcGwOgrILXX7+m4Og4AKUVrJtLutVVAMAg9dkbbywoyLFxAGormO1WRwEAA6rgzTdbe2AuyfFxAKr271VQAMBwKJhNcnwCgLIKzp+/RwoAGDIFa2fTraQCAIas/1y40Nra2SQnJgCorWCmWwEFAAyvgosXW1s7k+TkBAClFayZTXJiEoCqfX7xYmtrZpLu7hUAMDIK3nprQUFOTgJQW8GpdEtWAMAIKnj77Z+2NaeSnJoEoLCC1qZOdcnJKQCq9sU777Q2dSrJ54v9kf8C56XdmCPhH6kAAAAOZVhJZk1NACoAAAAIAAAAAAAAANJTkwAAAABJRU5ErkJggg=='></div>
                    <div class='button-text'>Windows</div>
                </button>
                <button type='submit' name='os' value='linux' class='button'>
                    <div class='logo'><img class='image-linux-logo' src='data:image/png;base64,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'></div>
                    <div class='button-text'>Linux</div>
                </button>
            </form>
        </div>
        <div class="loading-screen">
            <div class="spinner"></div>
            <div class="loading-text">En cours d'installation</div>
        </div>
    </div>
    <script>
        document.querySelectorAll('.button').forEach(button => {
            button.addEventListener('click', async function(e) {
                const loadingScreen = document.querySelector('.loading-screen');
                const mainContent = document.querySelector('.container');
                
                // Activer l'animation immédiatement
                mainContent.style.opacity = '0';
                loadingScreen.classList.add('active');

                try {
                    const formData = new URLSearchParams();
                    formData.append('os', this.value);

                    const response = await fetch('/install', {
                        method: 'POST',
                        body: formData,
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded'
                        }
                    });

                    const result = await response.text();
                    
                    if (!response.ok) throw new Error(result);

                    // Mise à jour du texte après succès
                    document.querySelector('.loading-text').textContent = "Installation réussie !";
                    await new Promise(resolve => setTimeout(resolve, 2000));
                    
                    // Redirection ou autre action
                    window.location.reload();

                } catch (error) {
                    loadingScreen.style.backgroundColor = 'rgba(80, 0, 0, 0.95)';
                    document.querySelector('.spinner').style.display = 'none';
                    document.querySelector('.loading-text').innerHTML = 
                        `Erreur : ${error.message}<br><small>Réessayez ou contactez le support</small>`;
                }
            });
        });
    </script>

    <script>
        function selectWindows(os) {
            alert('Vous avez choisi : Windows');
        }
        function selectLinux(os) {
            alert('Vous avez choisi : Linux');
        }
    </script>
    <script type='text/javascript' src='https://code.jquery.com/jquery-1.7.1.min.js'></script>
    <script src='assets/js/navbar.js'></script>
</body>
</html>
            """;
            
            

        byte[] responseBytes = htmlForm.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
