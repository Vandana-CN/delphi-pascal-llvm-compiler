PROGRAM Test4;

VAR
  a, b, result: INTEGER;

PROCEDURE greet;
BEGIN
  writeln('Hello from the procedure!');
END;

FUNCTION add(x, y: INTEGER): INTEGER;
BEGIN
  Add := x + y;
END;

BEGIN
  a := 10;
  b := 20;
  greet;;
  result := add(a,b);
  writeln('Sum is: ', result);
END.
