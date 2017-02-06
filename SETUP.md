Git Setup 
----------

* Fork this repository from github web-page

The following commands must be followed on your machine. 

* Clone the repository
```
git clone https://github.com/arjun-krishna/WhenBus.git

```
* Setting up remote configuration
```
git remote rename origin upstream

git remote add origin https://github.com/{user-name}/WhenBus.git

```
* Commands to use to get the latest source code 
```
  git fetch upstream master
  git rebase upstream/master
```

* To commit your changes to the code use  
```

git commit 

# This takes you into a Atom editor 
# Write meaning full commit message - can be multilined
# After the commit message is written
# Ctrl+O , ENTER, Ctrl+X to save and exit from the Atom editor

# For single line commit message you could use,
# git commit -m "{your-mssg}"

```
* Before pushing the code ensure that your code is upto
  date with the upstream/master
* To get upstream code changes do

```

 git fetch upstream master
 git rebase upstream/master
 
```
* If there are merge conflicts please resolve them,
  and commit the changes

* Finally, Push the changes to your repo  
```
 git push origin master
```

Now send a Pull request to arjun-krishna/WhenBus when you have
added new code, this will help keeping track of the code.

The Pull Request (PR) Procedure :

* Go to your github profile and to your repository page
https://github.com/{user-name}/WhenBus

* See the Pull request tab (https://github.com/{user-name}/WhenBus/pulls)

* Click on the "New pull request" button

* You can now see the changes you have made etc.,

* Submit the PR 
