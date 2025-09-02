declare i32 @printf(i8*, ...)
@print.str = constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
%i = alloca i32
store i32 1, i32* %i
br label %cond1
cond1:
%t5 = load i32, i32* %i
%t6 = icmp sle i32 %t5, 10
br i1 %t6, label %body2, label %end4
body2:
%t9 = load i32, i32* %i
%t10 = icmp eq i32 %t9, 3
br i1 %t10, label %then7, label %endif8
then7:
br label %incr3
br label %endif8
endif8:
%t13 = load i32, i32* %i
%t14 = icmp eq i32 %t13, 7
br i1 %t14, label %then11, label %endif12
then11:
br label %end4
br label %endif12
endif12:
%t15 = load i32, i32* %i
call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @print.str, i32 0, i32 0), i32 %t15)
br label %incr3
incr3:
%t16 = load i32, i32* %i
%t17 = add i32 %t16, 1
store i32 %t17, i32* %i
br label %cond1
end4:
ret i32 0
}
