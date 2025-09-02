declare i32 @printf(i8*, ...)
@print.str = constant [4 x i8] c"%d\0A\00"
@.str0 = constant [26 x i8] c"Hello from the procedure!\00"
@.str1 = constant [26 x i8] c"Hello from the procedure!\00"
@.str2 = constant [9 x i8] c"Sum is: \00"
define void @greet() {
entry:
call i32 (i8*, ...) @printf(i8* getelementptr ([26 x i8], [26 x i8]* @.str0, i32 0, i32 0))
ret void
}

define i32 @main() {
entry:
%a = alloca i32
%b = alloca i32
%result = alloca i32
store i32 10, i32* %a
store i32 20, i32* %b
call i32 (i8*, ...) @printf(i8* getelementptr ([26 x i8], [26 x i8]* @.str1, i32 0, i32 0))
call void @greet()
%add = alloca i32
%x = alloca i32
%t1 = load i32, i32* %a
store i32 %t1, i32* %x
%y = alloca i32
%t2 = load i32, i32* %b
store i32 %t2, i32* %y
%t3 = load i32, i32* %x
%t4 = load i32, i32* %y
%t5 = add i32 %t3, %t4
store i32 %t5, i32* %add
%t6 = load i32, i32* %add
store i32 %t6, i32* %result
call i32 (i8*, ...) @printf(i8* getelementptr ([9 x i8], [9 x i8]* @.str2, i32 0, i32 0))
%t7 = load i32, i32* %result
call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @print.str, i32 0, i32 0), i32 %t7)
ret i32 0
}
