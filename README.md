# ASMI - A JVM Based Language

ASMI was created for learning purposes only and is not intended to be used!

ASMI is a python-esque language built on JVM.

# CLI
## REPL
Without any arguments the CLI will start a REPL prompt. See also `Asmi` IntelliJ IDEA Run Configuration.

Note that only single-line statements are currently supported.

## Compile and Run Source
To compile and run a source file, pass the path as first argument to the CLI. See also `Asmi Source` IntelliJ IDEA Run Configuration.

# Language Functionality
## Print
```
print "Hello"
> Hello
```

## Arithmetic
```
print 1 + 2
> 3

print 2 + 3 * 4
> 14

print (2 + 3) * 4
> 20
```

## If-Else Conditions
```
if 1 + 1 == 2
  print "It's true"
else
  print "Not really"
end
```

## Variables
```
var numOne = 1
var numTwo = 2

if numOne * 2 == numTwo
  print "Variables can be used in expressions"
else
  print "Something went wrong"
end
```

## While loops
```
var i = 0
            
while i < 3
  print i
  i = i + 1
end
```

# References

- https://asm.ow2.io/
- http://craftinginterpreters.com/contents.html
- https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions