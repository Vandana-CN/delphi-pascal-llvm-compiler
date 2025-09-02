declare i32 @printf(i8*, ...)
@print.str = constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
%i = alloca i32
store i32 1, i32* %i
br label %cond1
cond1:
%t5 = load i32, i32* %i
%t6 = icmp sle i32 %t5, 5
br i1 %t6, label %body2, label %end4
body2:
%t7 = load i32, i32* %i
call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @print.str, i32 0, i32 0), i32 %t7)
br label %incr3
incr3:
%t8 = load i32, i32* %i
%t9 = add i32 %t8, 1
store i32 %t9, i32* %i
br label %cond1
end4:
ret i32 0
}
