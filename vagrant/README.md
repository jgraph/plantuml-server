# Generating PlantUML-Server War using Vagrant

1. Follow the steps in PlantUML repository to build and deploy PlantUML Jar file [README.md](https://github.com/jgraph/plantuml/blob/master/vagrant/README.md)
1. Run `vagrant up`.
1. Now, run `vagrant ssh` to login to the VM
1. We can now start the building process. Run `rm -rf plantuml-server; git clone https://github.com/jgraph/plantuml-server.git; cd plantuml-server; mvn package`.

Note: You can shutdown the VM using `vagrant halt`

# Using the same VM to deploy again

1. Start Vagrant VM `vagrant up`
1. Only last step is needed (`rm -rf plantuml-server; git clone https://github.com/jgraph/plantuml-server.git; cd plantuml-server; mvn package`).