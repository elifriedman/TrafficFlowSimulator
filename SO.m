cd('/home/friedm3/IdeaProjects/TrafficFlowSimulator')
costmat = load('matrix.csv');
costs = load('vector.csv');

SD = ones(1,3)
cvx_begin
    variable X(3,1) integer nonnegative
    minimize transpose(costmat*X + costs)*X;
    SD*X == 1500;
cvx_end
X
