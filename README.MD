# UNIVERSITA' DEGLI STUDI DI NAPOLI FEDERICO II
# WEB TECHNOLOGIES
## Author

    Raffaele D'Anna N97000455

# Meme Museum – Spring Boot Backend
<p align="center">
  <!-- Logo per tema chiaro -->
  <img src="src/main/resources/static/image/logo.png#gh-light-mode-only" alt="Meme Museum Logo" width="200"/>
  <!-- Logo per tema scuro -->
  <img src="src/main/resources/static/image/logo-dark.png#gh-dark-mode-only" alt="Meme Museum Logo Dark" width="200"/>
</p>


## Prerequisiti

Prima di iniziare, è essenziale assicurarsi di avere i seguenti strumenti e dipendenze installati sul tuo sistema:
1. Docker <img src="https://static.alphorm.com/blog-alphorm-uploads/2024/02/08164015/LogoDocker-Guide2024.png" alt="Docker Logo" width="60"/>

    Verifica la tua versione:
    ```bash
    docker --version
## Istruzioni per l'uso
1. **Avvio dei container docker per il database e il file system delle immagini**
   ```bash
   docker compose -p mememuseum-infrastructure up -d