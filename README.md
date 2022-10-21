
## Projeyi Başlatma

**Note:** Başlamadan önce bilgisayarınızın güvenli bir yerinde klasör oluşturun ve klasörü kullandığınız IDE (Android Studio vs.) üzerinde açın.
***

Projeyi klonlayın (Bir sefer yapmanız yeterli)

```bash
  git clone https://github.com/yunusemreyakisan/edithor-app.git
```

Proje dizinine gidin

```bash
  cd my-project (proje adına ne koyduysanız)
```
## Branch oluşturma ve isimlendirme

Proje üzerinde branch oluşturma (local)
```bash
  git checkout -b branchadiniz
  ornegin: git checkout -b issue#1-add-splashscreen
```

 ## Commit atmadan önce !

Git'e kendinizi tanıtın

```bash
  git config --global user.email "youruseremail"
  git config --global user.name "yourname"
```

Git'i başlatın

```bash
  git init
```


Yaptığınız değişiklikleri git'e ekleyin (Eklemek istediğiniz dosyayı ekler)

```bash
  git add dosyaadi
```


Yaptığınız değişiklikleri git'e ekleyin (Hepsini ekler)

```bash
  git add .
```

Yaptığınız değişikliğe yorum ekleyin ve commitleyin

```bash
  git commit -m "degisiklikyorumun"
```


Kendinizi uzak sunucuya tanıtın.

```bash
 git remote add origin https://github.com/yunusemreyakisan/edithor-app.git
```

  Yaptığınız değişikliği uzak sunucuya gönderin.

```bash
 git push -u origin branchadiniz
```

-- Herhangi bir hata alırsanız repository sahibiyle iletişime geçiniz. 
