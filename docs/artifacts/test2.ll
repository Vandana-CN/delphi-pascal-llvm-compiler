declare i32 @printf(i8*, ...)
@print.str = constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
%x = alloca i32
store i32 1, i32* %x
br label %loop1
loop1:
%t4 = load i32, i32* %x
%t5 = icmp slt i32 %t4, 5
br i1 %t5, label %body2, label %end3
body2:
%t6 = load i32, i32* %x
call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @print.str, i32 0, i32 0), i32 %t6)
%t7 = load i32, i32* %x
%t8 = add i32 %t7, 1
store i32 %t8, i32* %x
br label %loop1
end3:
ret i32 0
}
