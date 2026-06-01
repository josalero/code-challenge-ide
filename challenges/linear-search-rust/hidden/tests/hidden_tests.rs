use challenge::linear_search;

#[test]
fn hidden_cases() {
    assert_eq!(linear_search(&[9], 9), 0);
    assert_eq!(linear_search(&[], 1), -1);
}
