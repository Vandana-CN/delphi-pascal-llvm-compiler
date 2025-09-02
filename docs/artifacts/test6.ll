declare i32 @printf(i8*, ...)
@print.str = constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
%x = alloca i32
%t1 = add i32 10, 20
store i32 %t1, i32* %x
%t2 = load i32, i32* %x
call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @print.str, i32 0, i32 0), i32 %t2)
ret i32 0
}
