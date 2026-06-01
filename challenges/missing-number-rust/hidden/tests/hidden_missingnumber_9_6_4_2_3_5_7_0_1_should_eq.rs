use challenge::missing_number;

#[test]
fn hidden_missingnumber_9_6_4_2_3_5_7_0_1_should_eq() {
    assert_eq!(missing_number(&[9, 6, 4, 2, 3, 5, 7, 0, 1]), true);
}
