# Instructions of using git-crypt

git-crypt is tool for encryption project secrets

Install it from author repository releases [see](https://github.com/AGWA/git-crypt/releases)

Also you need GnuPG to create private key pairs

Search in your package manager or install from [gnupg.org](https://www.gnupg.org/download/)

Tell git-crypt which files or folders it needs to monitor in ```.gitattributes```
```
# One specific file
secretfile filter=git-crypt diff=git-crypt

# Files that ends with .key
*.key filter=git-crypt diff=git-crypt

# And folder with all included files
secretdir/** filter=git-crypt diff=git-crypt
```

Check if the specified previously files will be encrypted
```
git crypt status -e
```

On your next ```git push``` command they will be encrypted

To decrypt files after cloning repository use
```
git crypt unlock
```

And for encryption use
```
git crypt lock
```

