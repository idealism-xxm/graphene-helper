# graphene-plugin

Improve your experience when using grahpene in IntelliJ IDEA and PyCharm.

### Features
- [x] Useful navigation between declaration and resolver in same class
- [x] Useful navigation from mutation to it's field in schema.py
- [x] Generate resolver with args for resolvable field
- [x] Generate mutation function with args for mutation subclass
![generation-and-navigation](images/generation-and-navigation.gif)

### Todo
- [ ] Support navigation between declaration and resolver in superclass or subclass
- [ ] Custom search files for navigation of mutation
- [ ] ~~Support reference of mutation field for navigation of mutation~~ (not support because it's hard to get it's reference of mutation class)
- [ ] Support display identifier name when multiple info found