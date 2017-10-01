PrivateStorage
===

(GPL) Android PrivateStorage is a FREE software.

This application allows to create data binders by linking them to predefined actions:
- Copy in the clipboard
- Launch the default dialer
- Launch the default web browser
- Launch the default e-mail client


Except for actions, all data is stored using blowfish algorithm.


Instructions
============


download the software :

	mkdir devel
	cd devel
	
	git clone git://github.com/Keidan/PrivateStorage.git
	cd PrivateStorage
 	Use with android studio 
	
Important
-------
To make the blowfish algorithm work properly, you must edit the ``blowfish_cipher_key`` field (file ``PrivateStorage/application/src/main/res/values/strings.xml``) 

and replace ``YOUR_BLOWFISH_KEY_HERE`` with your private key.


License (like GPL)
==================
