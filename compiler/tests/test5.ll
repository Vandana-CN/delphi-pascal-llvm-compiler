declare i32 @printf(i8*, ...)
@print.str = constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
%x = alloca i32
%y = alloca i32
store i32 3, i32* %x
store i32 99, i32* %y
%t4 = load i32, i32* %x
%t5 = icmp slt i32 %t4, 5
br i1 %t5, label %then1, label %else2
then1:
%t6 = load i32, i32* %x
call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @print.str, i32 0, i32 0), i32 %t6)
br label %endif3
else2:
%t7 = load i32, i32* %y
call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @print.str, i32 0, i32 0), i32 %t7)
br label %endif3
endif3:
ret i32 0
}
