UDPDrawingTool
====

## �T�v
Java�Ŏ����������G�`���ʐM�c�[���D

UDP�ʐM�ɂ��C1��1�̂��G�`�����L���\�ɂ��Ă���D

�`�����G�͉摜�Ƃ��ĕۑ����邱�Ƃ��\�ƂȂ��Ă���D




## �g�p���@
1. jar�t�@�C������N������ꍇ
  1. ���[�U1���N������

    ```sh
    $ java -jar Graphicstest.jar localhost 50000 50001
    ```

  2. ���[�U2���N������

    ```sh
    $ java -jar Graphicstest.jar localhost 50001 50000
    ```

2. class�t�@�C������N������ꍇ
  1. ���[�U1���N������

    ```sh
    $ java -classpath bin GraphicsTest localhost 50000 50001
    ```

  2. ���[�U2���N������

    ```sh
    $ java -classpath bin GraphicsTest localhost 50001 50000
    ```




## �r���h�菇
### Ant��p����ꍇ
�ȉ��̂悤�ɁC�g�b�v�f�B���N�g���ɂ���build.xml��p����Ƃ悢(�v:ant�R�}���h)�D

```sh
$ ant
```


### GNU Make��p����ꍇ
�ȉ��̂悤�ɁC�g�b�v�f�B���N�g���ɂ���Makefile��p����Ƃ悢(�v:make�R�}���h)�D

```sh
$ make
```


### �蓮�Ńr���h����ꍇ
�ȉ��̃R�}���h�ŃR���p�C�����邱�Ƃ��ł���D

```sh
$ javac src/GraphicsTest.java -d bin -encoding utf-8
```

Jar�t�@�C���̍쐬���@�ɂ��ẮC��������D




## Javadoc�̐���
�t����build.xml��Makefile��p����ƁC�ȒP��Javadoc�𐶐����邱�Ƃ��ł���D


### Ant��p����ꍇ
�ȉ��̂悤�ɁCant�R�}���h�̈�����"javadoc"��^����ƁCjavadoc�f�B���N�g���Ƀh�L�������g�����������D

```sh
$ ant javadoc
```


### Gnu Make��p����ꍇ
�ȉ��̂悤�ɁCmake�R�}���h�̈�����"javadoc"��^����ƁCjavadoc�f�B���N�g���Ƀh�L�������g�����������D

```sh
$ make javadoc
```


## LICENSE

This software is released under the MIT License, see [LICENSE](LICENSE).
