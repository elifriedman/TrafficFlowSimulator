costmat = load('../simulations/OWNet/cost_mat.csv');
costs = load('../simulations/OWNet/costs.csv');

% Braess-1
% costmat = [0.01 0 0.01; 0 0.01 0.01; 0.01 0.01 0.02];
% costs = [45; 45; 0];

% SimpleNet
% costmat = [0 0; 0 0.1];
% costs = [40; 20];

T = 1700;
cvx_begin quiet
    variable X(length(costs),1) integer nonnegative
    final_costs = (costmat*X+costs);
    minimize transpose(X)*final_costs
    sum(X)==T;
cvx_end

final_costs = (costmat*X+costs);
total_cost = final_costs' * X / T;
final_X = X;
disp(['Normal: ',num2str(T),' (',num2str(total_cost),')'])
disp(num2str(final_costs','%g\t'))
disp(num2str(X','%g\t'))
disp('  ')
csvwrite('../simulations/OWNet/optima',final_X)