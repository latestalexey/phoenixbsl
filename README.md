# Phoenix BSL for 1s

**В СТАДИИ РАЗРАБОТКИ, ИСПОЛЬЗУЙТЕ НА СВОЙ СТРАХ И РИСК =)**

## Описание

Проект позволяет из 1С запускать проверку текста модуля и отформатировать текст модуля 
с помощью проекта [BSL LS](https://github.com/1c-syntax/bsl-language-server).

## Быстрый старт

Запускается ТОЛЬКО НА **Java 11** или через установку **msi**.

Собираем jar файл или берем из релизов (если там есть таковые). Запускаем в консоли:
```bash
java -jar /path/to/file/jar
```

В 1С:
* Запуска проверки текста модуля и вывод на экран -> нажимаем `CTRL` + `I`.
* Отформатировать текст модуля -> нажимаем `CTRL` + `K`.

## Разработка

Для доработки проекта, нужно использоваться Java 11.

## Развитие
Есть есть какие то идеи, касаемо проекта -> кидаем в раздел 
[Issues](https://github.com/otymko/phoenixbsl/issues).


**P.S.** Зачем это, если есть Снегопат, Turboconf, SmartConfigurator и т.п.? 
Ответ -> использование языка Java, открытый исходный код, прокачка в разработке на Java.
