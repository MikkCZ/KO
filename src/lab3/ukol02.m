function ukol02()
    hours = 24;
    shift = 8;
    sense = 1; %sense of optimization: 1=minimization, -1=maximization
    
    b = [6 6 6 6 6 8 9 12 18 22 25 21 21 20 18 21 21 24 24 18 18 18 12 8]';
    seminar3_A = seminar3_generateA(hours, shift);
    [c, ctype, lb, ub, vartype] = seminar3_generateOther(hours, 'G');
    [seminar3_xmin,seminar3_status] = solve(sense,c,seminar3_A,b,ctype,lb,ub,vartype);
    
    b = [b;-b];
    ukol02_A = ukol02_generateA(hours, shift);
    [c, ctype, lb, ub, vartype] = ukol02_generateOther(hours, 'L');
    [ukol02_xmin,ukol02_status] = solve(sense,c,ukol02_A,b,ctype,lb,ub,vartype);
    
    %show the solution
    if(seminar3_status==1 && ukol02_status==1)
        disp('Solution ukol02: '); disp(ukol02_xmin)
        disp('Solution seminar3: '); disp(seminar3_xmin)
        disp('Objective function ukol02: '); disp(sum(ukol02_xmin(1:hours)))
        disp('Objective function seminar3: '); disp(sum(seminar3_xmin(1:hours)))
        figure('position', [0, 0, 1300, 650]);
        bGraph = bar([ukol02_A(1:hours,1:hours)*ukol02_xmin(1:hours) b(1:hours) seminar3_A*seminar3_xmin],0.5);
        bGraph(1).FaceColor = 'magenta'; % ukol
        bGraph(2).FaceColor = 'green'; % pozadavky
        bGraph(3).FaceColor = 'yellow'; % seminar
    else
        disp('No feasible solution found!');
    end;
end

function [xmin,status] = solve(sense,c,A,b,ctype,lb,ub,vartype)
    %optimization parameters
    schoptions=schoptionsset('ilpSolver','glpk','solverVerbosity',2);
    %call command for ILP
    [xmin,fmin,status,~] = ilinprog(schoptions,sense,c,A,b,ctype,lb,ub,vartype);
    disp('fmin: '); disp(fmin)
end

function A = ukol02_generateA(hours, shift)
    A = seminar3_generateA(hours, shift);
    A = [A zeros(hours,hours)];
    A = [A;-A];
    for i=1:hours
        A(i,i+hours) = -1;
        A(i+hours,i+hours) = -1;
    end
end

function A = seminar3_generateA(hours, shift)
    A = zeros(hours,hours);
    for i=1:hours
        for j=1:hours
            if i<=(shift-1)
                if j<=i || j>=i+(hours-(shift-1))
                    A(i,j) = 1;
                end
            else
                if j>=i-(shift-1) && j<=i
                    A(i,j) = 1;
                end
            end
        end
    end
end

function [c, ctype, lb, ub, vartype] = ukol02_generateOther(hours, contype)
    [~, ctype, lb, ub, vartype] = seminar3_generateOther(2*hours, contype);
    c = [zeros(1,hours) ones(1,hours)]; %kriterialni fce c'x
end

function [c, ctype, lb, ub, vartype] = seminar3_generateOther(hours, contype)
    c = ones(1,hours); %kriterialni fce c'x
    ctype = char(zeros(hours,1));
    lb = zeros(hours,1); %lower bound of the variables
    ub = zeros(hours,1);
    vartype = char(zeros(hours,1));
    for i=1:hours
        ctype(i) = contype; %constraint type: 'E'="=", 'L'="<=", 'G'=">="
        ub(i) = 1000; %upper bound of the variables
        vartype(i) = 'I'; %variable type: 'C'=continuous, 'I'=integer
    end
end